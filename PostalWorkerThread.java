/*
Implementation of postal worker thread class.

Rules: 
    -serves next customer in line
    -service time: 
        -buy stamps = 1 sec
        -mail a letter = 1.5 sec
        -mail a package = 2 sec
    -scale: 
        -only used to mail a package
        -only used by one worker at a time

Actions: 
    1. worker serves next customer
    2. worker uses and releases scale if needed
    3. worker finishs serving customer
*/

public class PostalWorkerThread implements Runnable {
    
    private int workerIndex, custIndex, task;
    
    PostalWorkerThread(int workerIndex) {   //constructor
        this.workerIndex = workerIndex;
    }    
    
    @Override
    public void run() {
        
        System.out.println("Postal worker " + this.workerIndex + " created");
        
        while (true) {  //keep serving next available customer
            
            try {
                Main.cust_enter.acquire();  //wait(cust_enter)
            } catch (InterruptedException e) {}
            
            //critical region
            try {
                Main.mutex2.acquire();  //wait(mutex2)
            } catch (InterruptedException e) {}
            custIndex = Main.line.poll();   //get assigned customer index 
            task = Main.line.poll();    //get customer task
            Main.workerList.add(workerIndex);   //add worker index to the list
            Main.worker_enqueue.release();  //signal(worker_enqueue)
            Main.mutex2.release();  //signal(mutex2)
            
            //serve()
            System.out.println("Postal worker " + workerIndex + " serving customer " + custIndex);
            Main.worker_serve.release();    //signal(worker_serve)
            
            try {
                Main.cust_ask[custIndex].acquire();    //wait(cust_ask)
            } catch (InterruptedException e) {}
            
            //working on task
            if (task == CustomerThread.BUY_STAMPS) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
            else if (task == CustomerThread.MAIL_LETTER) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {}
            }
            else { 
                try {
                    Main.scale.acquire();   //wait(scale)
                    //useScale()
                    System.out.println("Scales in use by postal worker " + workerIndex);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
                
                //releaseScale()
                System.out.println("Scales released by postal worker " + workerIndex);
                Main.scale.release();   //signal(scale)
            }
            
            //finishServe()
            System.out.println("Postal worker " + workerIndex + " finished serving customer " + custIndex);
            Main.finish[custIndex].release();   //signal(finish[custIndex])
            
            try {
                Main.cust_finish[custIndex].acquire(); //wait(cust_finish)
            } catch (InterruptedException e) {} 
            Main.workers.release();     //signal(workers)
        }   
    }
}
