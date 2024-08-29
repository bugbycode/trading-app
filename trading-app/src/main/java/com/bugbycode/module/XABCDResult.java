package com.bugbycode.module;

public class XABCDResult<X extends Klines,A extends Klines,B extends Klines,C extends Klines,D extends Klines,FIB extends FibInfo> {

	private X x;
	
	private X a;
	
	private X b;
	
	private X c;
	
	private X d;
	
	private FIB fib; 

	public XABCDResult(X x, X a, X b, X c, X d, FIB fib) {
		this.x = x;
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.fib = fib;
	}

	public X getX() {
		return x;
	}

	public X getA() {
		return a;
	}

	public X getB() {
		return b;
	}

	public X getC() {
		return c;
	}

	public X getD() {
		return d;
	}

	public FIB getFib() {
		return fib;
	}

	@Override
	public String toString() {
		
		QuotationMode qm = fib.getQuotationMode();
		
		StringBuffer buffer = new StringBuffer();
		
		switch (qm) {
		
		case LONG:
			
			buffer.append("X=" + (x == null ? 0 : x.getLowPrice()));
			buffer.append(",");
			buffer.append("A=" + (a == null ? 0 : a.getHighPrice()));
			buffer.append(",");
			buffer.append("B=" + (b == null ? 0 : b.getLowPrice()));
			buffer.append(",");
			buffer.append("C=" + (c == null ? 0 : c.getHighPrice()));
			buffer.append(",");
			buffer.append("D=" + (d == null ? 0 : d.getLowPrice()));
			
			break;

		default:
			
			buffer.append("X=" + (x == null ? 0 : x.getHighPrice()));
			buffer.append(",");
			buffer.append("A=" + (a == null ? 0 : a.getLowPrice()));
			buffer.append(",");
			buffer.append("B=" + (b == null ? 0 : b.getHighPrice()));
			buffer.append(",");
			buffer.append("C=" + (c == null ? 0 : c.getLowPrice()));
			buffer.append(",");
			buffer.append("D=" + (d == null ? 0 : d.getHighPrice()));
			
			break;
		}
		
		return buffer.toString();
	}
	
}
