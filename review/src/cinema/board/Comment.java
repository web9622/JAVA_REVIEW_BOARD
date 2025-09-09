package cinema.board;

import java.util.Date;

import lombok.Data;

@Data
public class Comment {
private int cno;
private int bno;
private String userId;
private String content;
private Date cdate;
}
