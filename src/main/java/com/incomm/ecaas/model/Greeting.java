package com.incomm.ecaas.model;

import java.io.Serializable;

public class Greeting implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2236437920816681585L;
	private long id;
    private String content;
    
    public Greeting() {    	
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Greeting [id=").append(id).append(", content=").append(content).append("]");
		return builder.toString();
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
