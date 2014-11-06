import java.lang.Thread.State;

public class Calculator implements Runnable{
	private int number;

	public Calculator(int number){
		this.number = number;
	}

	@Override
	public void run(){
		for(int i = 1; i <= 10; ++i){
			System.out.printf("%s: %d * %d = %d\n", Thread
				.currentThread().getName(), number, i, i*number);
		}
	}

	public static void main(String[] args){
		Thread threads[] = new Thread[10];
		Thread.State[] states = new Thread.State[10];

		for(int i = 0; i < 10; ++i){
			threads[i] = new Thread(new Calculator(i+1));
			if( i % 2 == 0){
				threads[i].setName("thread " + i);
				threads[i].setPriority(Thread.MAX_PRIORITY);
			}
			else{
				threads[i].setName("thread_" + i);
				threads[i].setPriority(Thread.MIN_PRIORITY);
			}
			states[i] = threads[i].getState();
			System.out.printf("%s: %si\n",
				threads[i].getName(), threads[i].getState());
			threads[i].start();
		}
		boolean finish = false;
		while(!finish){
			finish = true;
			for(int i = 0; i < 10; ++i){
				System.out.printf("%s: %s\n",
					threads[i].getName(), threads[i].getState());
				finish = finish && (threads[i].getState() == State.TERMINATED);
			}
		}
		
	}
}
