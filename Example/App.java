// App.java

public class App
{
  static public void main( String args[] ) throws Exception {
    System.out.println( "This is your application." );
    System.out.print( "Args: " );
    for (int a=0; a<args.length; ++a)
      System.out.print( args[a]+" " );
    System.out.println( "" );
    
    new App().new AppChild().print();
    
    new Foo();
    new Bar();
  }
  
  public class AppChild{
	  public void print(){
		  System.out.println("haha ....");
	  }
  }
}

