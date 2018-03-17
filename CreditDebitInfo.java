import java.util.*;
class CreditDebitInfo
{
	class Counter extends Thread
	{
		int time;
		CreditDebitInfo obj;
		Counter(int time,CreditDebitInfo obj)
		{
			this.time = time;
			this.obj = obj;
			this.start();
		}
		public void run()
		{
			int count = time-1;
			while(count>0)
			{
				try
				{
					this.sleep(1000);
					System.out.print(count+" ");
					count--;
				}
				catch(Exception e){}
			}
			System.out.println(" ");
			obj.idVerification();
		}
	}
	static Scanner in = new Scanner(System.in);
	long id,accountNo;
	int attempts,digits;
	String issuer,MII;
	boolean valid;
	CreditDebitInfo()
	{
		attempts = 0;
	}
	public void idVerification()
	{
		if(attempts>2)
		{
			System.out.print("Wait seconds ...30 ");
			attempts-=1;
			id = 0;
			new Counter(30,this);
			try
			{
				Thread.sleep(30*1000);
			}
			catch(Exception e){}
		}
		else
		{
			System.out.print("Enter Credit/Debit Card Number:");
			String idnum = in.nextLine();
			try
			{
				id = Long.parseLong(idnum);
			}
			catch(NumberFormatException e)
			{
				attempts++;
				System.out.println("Incorrect Input.");
				this.idVerification();
			}
		}
	}
	public boolean formatCheck()
	{
		long temp = this.id,fdigit=0,fsdigit=0;
		this.digits = 0;
		while(temp>0)
		{
			if(temp<10 && temp>0)
			{
				fdigit = temp;
			}
			if(temp>99999 && temp<1000000)
			{
				fsdigit = temp;
			}
			digits++;
			temp/=10; 
		}
		if(digits>16 || digits<13)
		{
			System.out.println("Card is not a Valid.");
			return false;
		}
		temp = this.id;
		if(fdigit==1 || fdigit==2)
			this.MII = "Airlines";
		else if(fdigit==3)
			this.MII = "Travel";
		else if(fdigit==4 || fdigit==5)
			this.MII = "Banking and Financial";
		else if(fdigit==6)
			this.MII = "Merchandising and Banking/Financial";
		else if(fdigit==7)
			this.MII = "Petroleum";
		else if(fdigit==8)
			this.MII = "Healthcare, Telecommunications";
		else if(fdigit==9)
			this.MII = "National Assignment";
		else 
			return false;
		
		if(digits==16)
			if(fdigit==4)issuer = "Visa";
			else if(fdigit==5)issuer = "MasterCard";
			else if(fdigit==6)issuer = "Discover";
			else issuer = "Diners Club";
		else if(digits==15)
			if(fdigit==3)issuer="American Express";
			else issuer = "Diners Club";
		else if(digits==14)
			issuer = "Diners Club";
		else
			issuer = "Visa";
		
		int sum=0;
		for(int i=1;i<=digits;i++){
			int a = ((i%2==0)?2:1);
			int t = (int)(temp%10);
			int s=0;
			s = (int)(a*t);
			if(a==2&&a*t>=10){
				int x = s%10;
				s = (s/10)+x;
			}
			sum+=s;
			temp = temp/10;
		}
		if(sum%10==0)
			valid = true;
		else valid = false;
		temp = this.id;
		String acnt = Long.toString(this.id),real="";
		for(int i=0;i<digits;i++){
			if(i>5 && i<digits-1){
				real+=acnt.charAt(i);
			}
		}
		accountNo = Long.parseLong(real);
		return true;
	}
	public static void main(String args[]){
		CreditDebitInfo cardinfo = new CreditDebitInfo();
		cardinfo.idVerification();
		cardinfo.formatCheck();
		System.out.println("Industry:"+cardinfo.MII);
		System.out.println("Issuer/brand:"+cardinfo.issuer);
		System.out.println("Account Number:"+cardinfo.accountNo);
		System.out.println("Valid:"+cardinfo.valid);
	}
}