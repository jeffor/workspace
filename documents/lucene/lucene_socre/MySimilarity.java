package org.apache.lucene.search.similarities;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

public abstract class MySimilarity extends Similarity {
    /** 缓存解码字节 */
    private static final float[] NORM_TABLE = new float[256];

    static {
        for (int i = 0; i < 256; i++) {
            NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte) i);
        }
    }

    public MySimilarity() {
    }

    @Override
    /**协调因子*/
    public float coord(int overlap, int maxOverlap) {
        return overlap / (float) maxOverlap;
    }

    @Override
    /**查询因子*/
    public float queryNorm(float sumOfSquaredWeights) {
        return 1f;
    }

    /**
     * 逆向文档频率
     *
     * @param docFreq
     *            该 field:term 在索引中匹配到的文档数目
     * @param numDocs
     *            包含目标 field 的所有文档数目
     *            <p>
     *            Invert Document Frequency =
     *            <code>log(numDocs/(docFreq+1)) + 1</code>
     *            </p>
     * @return idf value
     */
    public float idf(long docFreq, long numDocs) {
        return (float) (Math.log(numDocs / ((double) docFreq + 1)) + 1.0);
    }

    /** 逆向文档频率 */
    public Explanation idfExplain(CollectionStatistics collectionStats,
            TermStatistics termStats) {
        final long df = termStats.docFreq();
        final long max = collectionStats.maxDoc();
        final float idf = idf(df, max);
        return new Explanation(idf, "idf(docFreq=" + df + ", maxDocs=" + max
                + ")");
    }

    public Explanation idfExplain(CollectionStatistics collectionStats,
            TermStatistics termStats[]) {
        final long max = collectionStats.maxDoc();
        float idf = 0.0f;
        final Explanation exp = new Explanation();
        exp.setDescription("idf(), sum of:");
        for (final TermStatistics stat : termStats) {
            final long df = stat.docFreq();
            final float termIdf = idf(df, max);
            exp.addDetail(new Explanation(termIdf, "idf(docFreq=" + df
                    + ", maxDocs=" + max + ")"));
            idf += termIdf;
        }
        exp.setValue(idf);
        return exp;
    }

    @Override
    /**计算当前field的boost norm(索引时设置的提权信息)
     *@param state 一个 field 的反向索引信息对象，包含该域的索引信息
     *<p><code>norm = documentBoost * fieldBoost * lengthNorm</code>
     *由于商品信息普遍简短,lengthNorm不具有权重特征,因此在此只提取boost值</p>
     *@return 返回field的norm值
     */
    public final long computeNorm(FieldInvertState state) {
        return encodeNormValue(state.getBoost());
    }

    /** 解码存在索引中的 标准化因子 */
    public float decodeNormValue(long norm) {
        return NORM_TABLE[(int) (norm & 0xFF)];
    }

    /** 编码标准化因子用于保存至索引文件中 */
    public long encodeNormValue(float f) {
        return SmallFloat.floatToByte315(f);
    }

    @Override
    /**计算Weight对象*/
    public final SimWeight computeWeight(float queryBoost,
            CollectionStatistics collectionStats, TermStatistics... termStats) {
        final Explanation idf = termStats.length == 1 ? idfExplain(
                collectionStats, termStats[0]) : idfExplain(collectionStats,
                termStats);
        return new IDFStats(collectionStats.field(), idf, queryBoost);
    }

    @Override
    /**计算SimScore对象*/
    public final SimScorer simScorer(SimWeight stats,
            AtomicReaderContext context) throws IOException {
        IDFStats idfstats = (IDFStats) stats;
        return new TFIDFSimScorer(idfstats, context.reader().getNormValues(
                idfstats.field));
    }

    private final class TFIDFSimScorer extends SimScorer {
        private final IDFStats stats;                // field:term 的权重信息统计对象
        private final float weightValue;             // queryWeight
        private final NumericDocValues norms;        // document-field 标准化因子(索引时存储于文档中)

        TFIDFSimScorer(IDFStats stats, NumericDocValues norms)
                throws IOException {
            this.stats = stats;
            this.weightValue = stats.value;
            this.norms = norms;
        }

        @Override
        public float score(int doc, float freq) {
            // 原意为计算每篇文档 的 termFreq * value
            // final float raw = tf(freq) * weightValue;
            return norms == null ? weightValue : weightValue
                    * decodeNormValue(norms.get(doc));
        }

        @Override
        /**计算模糊匹配因子, 在 TF-IDF 中无意义*/
        public float computeSlopFactor(int distance) {
            return 1f;
        }

        @Override
        /**计算负载因子, 默认不计算*/
        public float computePayloadFactor(int doc, int start, int end,
                BytesRef payload) {
            return 1f;
        }

        @Override
        public Explanation explain(int doc, Explanation freq) {
            return explainScore(doc, freq, stats, norms);
        }
    }

    /** TF-IDF 的 Weight对象, 该对象是针对一个query term在此索引的一个field的统计数据 */
    private static class IDFStats extends SimWeight {
        private final String field;        // field name
        /** The idf and its explanation */
        private final Explanation idf;     // query term 的idf
        private float queryNorm;
        private float queryWeight;         // query term 的 权重
        private final float queryBoost;    // query term 的 boost值
        private float value;               // 缓存统计结果

        /**
         * 评分公式:
         * <code>score(q,d) = coord(q, d) * ∑(t in q){idf(t)^2 * t.getBoost() * norm(t, d)}</code>
         * */
        public IDFStats(String field, Explanation idf, float queryBoost) {
            // TODO: Validate?
            this.field = field;
            this.idf = idf;
            this.queryBoost = queryBoost;
            // compute query weight
            this.queryWeight = idf.getValue() * queryBoost; // idf(t)*t.getBoost()
        }

        @Override
        /**该方法本意为计算 sumOfSquaredWeights 的term项weight,
         * 而queryNorm如今恒等于1,因此该项无需计算*/
        public float getValueForNormalization() {
            return 1f;
        }

        @Override
        public void normalize(float queryNorm, float topLevelBoost) {
            // queryWeight * idf(t)
            value = topLevelBoost * queryWeight * idf.getValue();
        }
    }

    /**
     * 评分描述对象
     * 
     * @param doc
     *            文档ID
     * @param freq
     * @param stats
     * */
    private Explanation explainScore(int doc, Explanation freq, IDFStats stats,
            NumericDocValues norms) {
        Explanation result = new Explanation();
        result.setDescription("score(doc=" + doc + ",freq=" + freq
                + "), product of:");

        // explain query weight
        Explanation queryExpl = new Explanation();
        queryExpl.setDescription("queryWeight, product of:");

        Explanation boostExpl = new Explanation(stats.queryBoost, "boost");
        if (stats.queryBoost != 1.0f)
            queryExpl.addDetail(boostExpl);
        queryExpl.addDetail(stats.idf);

        Explanation queryNormExpl = new Explanation(stats.queryNorm,
                "queryNorm");
        queryExpl.addDetail(queryNormExpl);

        queryExpl.setValue(boostExpl.getValue() * stats.idf.getValue()
                * queryNormExpl.getValue());

        result.addDetail(queryExpl);

        // explain field weight
        Explanation fieldExpl = new Explanation();
        fieldExpl.setDescription("fieldWeight in " + doc + ", product of:");
        fieldExpl.addDetail(stats.idf);

        Explanation fieldNormExpl = new Explanation();
        float fieldNorm = norms != null ? decodeNormValue(norms.get(doc))
                : 1.0f;
        fieldNormExpl.setValue(fieldNorm);
        fieldNormExpl.setDescription("fieldNorm(doc=" + doc + ")");
        fieldExpl.addDetail(fieldNormExpl);

        fieldExpl.setValue(stats.idf.getValue() * fieldNormExpl.getValue());

        result.addDetail(fieldExpl);

        // combine them
        result.setValue(queryExpl.getValue() * fieldExpl.getValue());

        if (queryExpl.getValue() == 1.0f)
            return fieldExpl;

        return result;
    }
}

