package sprime.server

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.bluetooth.*;
import javax.microedition.io.*;

import sprime.server.PowerUsage;

public class Receiving implements Runnable {

    private double k = 0.70;		// percent of new value to believe
	private double min_zero = 511;	// actual no current value
    private double avgDisplay = min_zero;// start at about zero
    
    private StreamConnection connection = null;
    private InputStream inStream = null;
    private boolean stop = false;

    public Receiving(StreamConnection c) {
        connection = c;
		try {
		        inStream = connection.openInputStream();
		} catch (final Exception e) {
		        e.printStackTrace();
		}
    }

    @Override
    public void run() {
        while (!this.stop) {
            try {
				BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
				String lineRead = bReader.readLine();
				System.out.println("Server recv: " + lineRead);

                int watt = Integer.parseInt(lineRead);
                if(watt>=min_zero){
                	avgDisplay = (watt * k) + avgDisplay * (1 - k);
                }else{
                	avgDisplay = (min_zero * k) + avgDisplay * (1 - k);
                }
				
				// ~10 W per arduino division @5v sensor input
				double val = (avgDisplay-min_zero) ;
				if(val<0) val = 0;
				//val = val * 10;
				val = val * 9.0; // fiddle with scalar
				
				// display graph
                PowerUsage usage = new PowerUsage(wattage: val);
                usage.save();
				Thread.sleep(666);
            } catch (final Exception e) {
                e.printStackTrace();
            }
		}
    }

    public void stop() {
        this.stop = true;
    }
} // class

