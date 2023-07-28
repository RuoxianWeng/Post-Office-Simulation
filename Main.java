/*
By Ruoxian Weng on 03/2023.

This program uses threads to simulate customer and worker behavior in a post office.
It achieves mutual exclusion and coordination using semaphores.
*/  

import java.util.concurrent.Semaphore;
import java.util.*;

public class Main {
    
    //semaphores
    public static Semaphore max_capacity = new Semaphore(10, true);
    public static Semaphore workers = new Semaphore(3, true);
    public static Semaphore scale = new Semaphore(1, true);
    public static Semaphore mutex1 = new Semaphore(1, true);
    public static Semaphore mutex2 = new Semaphore(1, true);
    public static Semaphore cust_enter = new Semaphore(0, true);
    public static Semaphore worker_serve = new Semaphore(0, true);
    public static Semaphore worker_enqueue = new Semaphore(0, true);
    public static Semaphore cust_ask[] = new Semaphore[50];
    public static Semaphore finish[] = new Semaphore[50];
    public static Semaphore cust_finish[] = new Semaphore[50];
    
    //queues
    public static Queue<Integer> line = new LinkedList<Integer>();
    public static Queue<Integer> workerList = new LinkedList<Integer>();
    
    public static void main(String args[]) {
        
        int totalCustomers = 50;
        int totalWorkers = 3;
        
        //declare array of customer and worker objects
        CustomerThread customer[] = new CustomerThread[totalCustomers];
        Thread custThread[] = new Thread[totalCustomers];
        PostalWorkerThread worker[] = new PostalWorkerThread[totalWorkers];
        Thread workerThread[] = new Thread[totalWorkers];
        
        //initializing semaphores for each element in the array
        for (int i=0; i<totalCustomers; i++) {
            finish[i] = new Semaphore(0, true);
            cust_ask[i] = new Semaphore(0, true);
            cust_finish[i] = new Semaphore(0, true);
        }
        
        System.out.println("Simulating Post Office with " + totalCustomers + " customers and " + totalWorkers + " postal workers\n");
        
        //create 3 threads for postal workers
        for (int i=0; i<totalWorkers; i++) {
            worker[i] = new PostalWorkerThread(i);
            workerThread[i] = new Thread(worker[i]);
            workerThread[i].start();
        } 
        
        //create 50 threads for customers
        for (int i=0; i<totalCustomers; i++) {
            customer[i] = new CustomerThread(i);
            custThread[i] = new Thread(customer[i]);
            custThread[i].start();
        }   

        //join customer threads
        for (int i=0; i<totalCustomers; i++) {
            try {
                custThread[i].join();
                System.out.println("Joined customer " + i);
            } catch (InterruptedException e) {}
        }
    }
}

