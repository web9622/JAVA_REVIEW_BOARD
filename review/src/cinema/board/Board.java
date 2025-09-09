package cinema.board;

import java.sql.Blob;
import java.util.Date;
import lombok.Data;

@Data
public class Board {
private int bno;
private String btitle;
private String bwriter;
private Date bdate;
private int blike;
private String bcontent;
private String bfilename;
private Blob bfiledata;

}
