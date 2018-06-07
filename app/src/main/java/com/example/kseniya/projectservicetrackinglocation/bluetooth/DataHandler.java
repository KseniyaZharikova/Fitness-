package com.example.kseniya.projectservicetrackinglocation.bluetooth;

import java.util.Observable;

public class DataHandler extends Observable{
	private static DataHandler dd = new DataHandler();
	
	//DATA FOR SAVING
	boolean newValue = true;
	ConnectThread reader;
	H7ConnectThread H7;
	
	int pos=0;
	int val=0;
	int min=0;
	int max=0;
	
	//for the average maths
	int data=0;
	int total=0;

	int id;
	
	private DataHandler(){
		
	}
	
	public static DataHandler getInstance(){
		return dd;
	}

	public void acqui(int i){
		if (i==254){
			pos=0;
		}
		else if (pos==5){
			cleanInput(i);
		}
		pos++;
	}
	
	public void cleanInput(int i){
		val=i;
		if(val!=0){
			data+=val;//Average maths
			total++;//Average maths
		}
		if(val<min||min==0)
			min=val;
		else if(val>max)
			max=val;
		setChanged();
		notifyObservers();
	}

    public String getLastValue(){

        return val + " BPM";
    }

    public int getLastIntValue(){

        return val;
    }
	
	public String getMin(){
		return "Min " + min + " BPM";
	}
	
	public String getMax(){

		return "Max " + max + " BPM";
	}
	
	public String getAvg(){
		if(total==0)
            return "Avg " + 0 + " BPM";
		return "Avg " + data/total + " BPM";
	}




	public ConnectThread getReader() {
		return reader;
	}

	public void setReader(ConnectThread reader) {
		this.reader = reader;
	}

	public int getID() {
		return id;
	}
	public void setID(int id) {
		this.id=id;
	}

	public void setH7(H7ConnectThread H7){
		this.H7=H7;
	}
	public H7ConnectThread getH7(){
		return H7;
	}
	
}
