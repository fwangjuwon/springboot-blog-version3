package site.metacoding.blogv3.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UtilPost {
    
    //이 메소드의 책임: 이미지 태그 제거 !! 
    public static String getContentWithoutImg(String content) {

        //제이스프를 써보자
        Document doc = Jsoup.parse(content);

        //System.out.println(doc);
        //2. 실행
        Elements els = doc.select("img");

        for(Element el : els){
            el.remove();
        }
        return doc.select("body").text();
    }
}
