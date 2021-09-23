package com.scnu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.scnu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000118621170";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCmoK2PzQ5y3M/maso2whI/fnzUmWbFf9dFzdJ4E0YqpqWD/mwOkuIZ3weq30cHv+QgthzKgkxlLrtXtgEmjd5ZX4ceSTduhXnw89SWctRGISf7MfY5QkLHXkVruTVjwimAa1W7yEYyxgECUod9qd1wn7eiUT0PsIx3CveCdvFycSjlvX+GmQQzP6SE61kjZFIGH9UAdBrAMtqMc2wuLYQGn9jEGbUh7aUDEvonVfbRoVbhlT1DeflQXeBlbbRwXDyjgJR+57B6xO4y6e3lzvCvKXziUybQkOaB3O60epogqpAudX0KoLq0Q+pVeM1XmKf6KJOJ2jS40usSyhzK4Ux7AgMBAAECggEAb49IZOzfbZvVPenltT++Q/CF1zlVi4mKMdwZu/b7lXa5fmQLKL7Czpt6YLbYsaGSNSx4nkUarOva1mCu6Ic3hRLmPiq7CC4pdF9rx8bdxI5SXGYY+kEYwK6nP/ZifoXyj15NqknuZfQXnNxwhnwCHHNy2SQXA7gqc1FOYXUNNpbkGT/JkvXDG3g0BcICy0fBrq7/a4bDkGJJk2KW2ipK1bY/+j/RRlwJJOTQksXl1qYPgbi9PyVTdgszn1jjeabIIq+hS70ROVIOk4UXjFcAVN7djtXnqw+A4trV2aXIkL9hTUd/GIaAZ9I6qmtf4nFbNiBeZho90SKvbBMkgFlj4QKBgQDVmlQcKinZ7nC/JBugNM6oH8FQJmuTHeSuq1/E29SI6IJq7IlihMzqO591CsbOC23XDJPhOF3f4r8ScnZf+JHl+f8oZLZVzqPgk1aU2TtnNwbmVhN7f1oZ6zNQaiYR+67eU7QrQNz793C3wZhCwRvDzTwHujwbEIR0k0KyFbAjAwKBgQDHs21ivP8rGgveNrvb6EHIhxcVG+bz2stjfoWV6FSiY4CjaQP5Yc7gXst1YnC/PTkBjfD+jeatKGjHv/KQdx+vTQJNqY1zI7Dk4+vTVDUWIuxTd1RvxCHmm5Cnoll3aI6ahl++VJb3fgsobA0yHd9sigT9MXS/K5CiB5s0H947KQKBgDmYNkut1sELeN0hGE3XT02n2lEJOwJdEhdC4DzNZX5Q6zF3fAfTdQlHcFR3xmkyTbipjIoyz6UZl5mawW5ZMMXI5dL3bO6wHCHmuHbEKC9JeBPNa81l5l6Mu7ZYGNHKgjkyaqcrAyrxajQyACMnvJvMD/6paaGGj6B7QIWSMX8PAoGAXzn1ODctak0ySZVKAXQAInIglKVIfRHhGKGVa8Vszj6qccG67mkwcBdj56a4Ysj77PzVZV0fGYUCafXi4fab+ki7h3MJi1UCnjL9Gjm2Usb2cFqfvegFzmRyFy24gdMu5QqcjzLhBkVpZq4XoMVT6/ropB+4LwzEHC+KV9G2c/kCgYBtBemz9aI3W/wlNpqvBmMDJ490Epzev/YWW3HdTWTm368kSwnl5czcvHmm624AKkrR/o2CSjaAH6SLA8hJzhXfgKosXVdOEqxUpuLZewQp6GfHXaJ+0w9rY3MRogJe4CqNYv9mNVZWPZbSZkeHjhFOvxQ9kDxc65pZe5q/Ibbx0w==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj+rXkgoF4iEwU2VAvs71/z+ff5rc7BoCd5RVVlKUql2L7ysHX0lZEw3+ICadsqJueUkYym3XoBJwsz9IcsOdkhi4nl/XeEZ2Iq7zFxOhJkYnIGTRup20Y3/xhdcTp4euW0I4XcrdkrbPYTp7tAEOyyI2eGZ4IVifSGA4G7AZE5eHyb1RFmOILPGQRXtZeSUhumEH7aDf4ubn6+RdP2d3lKTDEHc84ncDcVAxBJ97IGl/vSGV99pQeWdJIE6je8TsdHtc5QPk7GYhcZIUCQHBbcDeNeKeHD4A+aSX42cw6KVrI30+y3Yu7SZ0syByW6XCa4c+Gg/Pu8l8Ky6tp3S7uQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息  natapp内网穿透 爱了
    private  String notify_url = "http://d68x2x.natappfree.cc/pay/callback";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/orderList.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ "1m" +"\","   //1分钟没有支付,就收单(订单取消) 感觉此处应该调用方法查询这个订单还有多久过期,再给支付宝设置时间
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
