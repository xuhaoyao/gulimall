import java.util.Arrays;
import java.util.List;

public class Test01 {
    public static void main(String[] args) {
        String s = "_6000";
        String[] s1 = s.split("_");
        System.out.println(s1.length);
        System.out.println(Arrays.toString(s1));
        System.out.println("".equals(s1[0]));
    }
}
