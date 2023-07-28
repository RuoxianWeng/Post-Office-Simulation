/*
Implementation of customer thread class.

Rules: 
    -only 10 customers can be inside the post office at a time
    -each customer is randomly assigned to one of the following tasks: 
        -buy stamps
        -mail a letter
        -mail a package

Actions: 
    1. customer created
    2. customer enters
    3. customer asks for help
    4. customer finishs the task
    5. customer leaves
*/

import java.util.Random;
        
public class CustomerThread implements Runnable {
    
    public static final int BUY_STAMPS = 0, MAIL_LETTER = 1, MAIL_PACKAGE = 2;  //constants
    private int custIndex, task, workerIndex;
    
    CustomerThread(int custIndex) { //constructor
        
        this.custIndex = custIndex; 
        
        //assign random task
        Random r = new Random();
        task = r.nextInt(3);    //get a random task from 0, 1, 2
    }    
    
    @Override
    public void run() {
        
        System.out.println("Customer " + this.custIndex + " created");

        try {
            Main.max_capacity.acquire();    //wait(max_capacity)
        } catch (InterruptedException e) {}
        
        //enter()
        System.out.println("Customer " + custIndex + " enters post office");
        
        //critical region that requires mutual exclusion
        try {
            Main.mutex1.acquire();  //wait(mutex1)
        } catch (InterruptedException e) {}
        Main.line.add(custIndex);   //add customer index in line queue
        Main.line.add(task);        //add customer task
        Main.cust_enter.release();  //signal(cust_enter)
        try {
            Main.worker_enqueue.acquire();  //wait(worker_enqueue)
        } catch (InterruptedException e) {}
        workerIndex = Main.workerList.poll();   //get assigned worker index
        Main.mutex1.release();  //signal(mutex1)
        
        try {
            Main.worker_serve.acquire();    //wait(worker_serve)
            Main.workers.acquire();     //wait(workers)
        } catch (InterruptedException e) {}
        
        String askAction = "";
        if (task == 0)
            askAction = "buy stamps";
        else if (task == 1)
            askAction = "mail a letter";
        else
            askAction = "mail a package";
        
        //ask()
        System.out.println("Customer " + custIndex + " asks postal worker " + workerIndex + " to " + askAction);
        Main.cust_ask[custIndex].release();    //signal(cust_ask)

        try {
            Main.finish[custIndex].acquire();   //wait(finish[custIndex])
        } catch (InterruptedException e) {}
        
        String finishAction = "";
        if (task == 0)
            finishAction = "buying stamps";
        else if (task == 1)
            finishAction = "mailing a letter";
        else
            finishAction = "mailing a package";

        //finishTask()
        System.out.println("Customer " + custIndex + " finished " + finishAction);
        Main.cust_finish[custIndex].release();     //signal(cust_finish)
        
        //leave()
        System.out.println("Customer " + custIndex + " leaves post office");
        Main.max_capacity.release();    //signal(max_capacity)
    }    
}
