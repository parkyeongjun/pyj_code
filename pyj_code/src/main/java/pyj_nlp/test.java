package pyj_nlp;

public class test {
	public static void main(String args[]) throws IndexOutOfBoundsException
	{
		String match = "[^\uAC00-\uD7A3xfea-zA-Z\\s^(--_)]"; // 유효성검사
		String a = "a_1b a-c";
		a = a.replaceAll(match, "");
		System.out.println(a);
	}          
}
