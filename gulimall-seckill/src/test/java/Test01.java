import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Test01 {

    @Test
    public void test01(){
        System.out.println(startDay());
        System.out.println(endDay(3));
    }

    private String startDay(){
        LocalDate today = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime dateTime = LocalDateTime.of(today,min);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endDay(Integer x){
        x = x < 1 ? 1 : x;
        LocalDate xDay = LocalDate.now().plusDays(x - 1);
        LocalTime max = LocalTime.MAX;
        LocalDateTime dateTime = LocalDateTime.of(xDay,max);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
