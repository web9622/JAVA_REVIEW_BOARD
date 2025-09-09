package cinema.board;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BoardEx {
	private String loginUserId=null;
	private Scanner scanner=new Scanner(System.in);
	Connection conn=null;
	//생성자
	public BoardEx() {
		
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			conn=DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521/orcl", 
					"gogoon1", 
					"1234"
					);
		}
		catch(Exception e) {
			e.printStackTrace();
			exit();
		}
		
	}
	
	
	public void joinList() {}
	
	//메인메뉴
	public void mainMenu() {
	do {	
		System.out.println(" ");
		System.out.println("✍ 홈");
        System.out.println("-------------------------------------------------------------------------------------");
		
		System.out.println("1.글작성 | 2.전체삭제 | 3.회원가입 | 4.로그인 | 5.게시물 목록 | 6.게시판 종료 | 7.조회 ");
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("[메뉴선택] : ");
		String menuNo=scanner.nextLine();
		System.out.println();
		
		//전체메뉴
		switch(menuNo) {
		case "1"-> {create();return;}
//		case "2"-> {if(loginUserId==null){
//			System.out.println("로그인해야 게시물을 읽을 수 있습니다.");
//			login();
//		}else {read();return;}}
		//case "2"-> {clear();return;}
		case "2"-> {if(loginUserId==null){
			System.out.println("로그인해야 게시물을 읽을 수 있습니다.");
			login();
		}else {clear();return;}}
		case "3"-> {join();return;}
		case "4"-> {
			if(loginUserId==null) {
				login();
				}else{
			logout();
			System.out.println("로그아웃 되었습니다.");
			list();}return;
		}
		case "5"-> {pagingList();return;}
		case "6"-> {exit();return;}
		case "7"-> {search();return;}
		default->{
			System.out.println("***1~7번호만 입력해 주세요.***");
		}
		}//swicth
	}//do
	while(true);
	}
	//공통되는 보조메뉴를 메소드를 뽑음
	private String printSubMenu() {
		//보조메뉴 출력
        System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("1.Ok | 2.Cancle");
		System.out.println("메뉴선택:");
		return scanner.nextLine();
	}
	
	//Likelist
	//데이터 null을 '-'로 대체
		private String safeString(String value) {
			return value != null ? value : "";
		}
	
	//페이징 있는 리스트
	public void pagingList() {
		final int pageSize=5;
		int totalPosts=0;
		int totalPages=0;
		int currentPage=1;
		try {
			String countSql="""
					SELECT COUNT(*) FROM cinemaboards
					""";
			PreparedStatement pstmt = conn.prepareStatement(countSql);
			ResultSet countRs=pstmt.executeQuery();
			if(countRs.next()) {
				totalPosts=countRs.getInt(1);
			}
			countRs.close();
			pstmt.close();
			if(totalPosts==0) {
				System.out.println("게시물이 없습니다.");
				mainMenu();
				return;
			}
			totalPages=(int)Math.ceil((double)totalPosts/pageSize);
			
			//페이징번호를 계속 입력받게 while문시킴
		while(true) {	
			//페이지별 시작rownum과 마지막rownum
			int start = (currentPage - 1) * pageSize + 1;
	        int end = currentPage * pageSize;
	        System.out.println();
	        System.out.println("[ 목록 ] "+"로그인:"+(loginUserId!=null?loginUserId:"게시물 상세보기는 로그인이 필요합니다."));
	        System.out.println("-------------------------------------------------------------------------------------");
	        System.out.printf("%-6s%-30s%-16s%-16s%-6s%-20s \n","no","제목","작성","작성일","좋아요","첨부파일");
	        System.out.println("-------------------------------------------------------------------------------------");
	        
	        String sql="""
	        		SELECT * FROM(
		        		SELECT ROWNUM rn,A.* FROM(
		        			SELECT bno,btitle,bwriter,bdate,blike,bfilename
		        			FROM CINEMABOARDS
		        			ORDER BY bno DESC
		        			)A
		        		WHERE ROWNUM <=?
	        		)
	        		WHERE rn>=?
	        		""";
	        pstmt= conn.prepareStatement(sql);
	      //받을 값은 숫자
	        pstmt.setInt(1, end);
	        pstmt.setInt(2, start);
	        ResultSet rs=pstmt.executeQuery();
	        
	        while(rs.next()) {
	        	//null을 공백으로 출력
	        	String bfilename=safeString(rs.getString("bfilename"));
	        	System.out.printf("%-6s%-30s%-16s%-16s%-6s%-20s",
	        		rs.getInt("bno"),
	        		rs.getString("btitle"),
	        		rs.getString("bwriter"),
	        		rs.getDate("bdate"),
	        		rs.getInt("blike"),
	        		//rs.getString("bfilename")
	        		bfilename
	        	
	        			);
	        	System.out.println();
	        }
	        	
	        rs.close();
	        pstmt.close();
	        
	        //페이지 네비게이션
	        System.out.println("-------------------------------------------------------------------------------------");
	        System.out.print("[페이지번호]");
	        for(int i=1; i<=totalPages; i++) {
	        	System.out.print(" ["+i+"]");
	        }
//	        System.out.println("페이지번호: ");
//	        String pageinput=scanner.nextLine();
	        System.out.println();
	        System.out.println("-------------------------------------------------------------------------------------");

	        //입력한 페이지 번호로 이동하기
	        System.out.println("[1.게시물 상세보기]'엔터키' | [2.페이지번호]'숫자입력' | [3.나가기] 'x 입력' ");
	       
	        String input=scanner.nextLine();
	        if(input.equals("0")) break;
	        //엔터쳐서 read()로 넘어가기
	        else if(input.equals("")) {
	        	if(loginUserId==null) {
	        		System.out.println("✎⁾⁾⁾ 로그인이 필요한 페이지입니다.");
	        		login();
	        	}else {
	        		read();
	        		continue;
	        		}
	        	}
	        else if(input.equals("x")) {
	        	mainMenu();
	        }else if (input.matches("\\d+")) { // 숫자인지 정규식으로 검사
	            int selectedPage = Integer.parseInt(input);
	            if (selectedPage >= 1 && selectedPage <= totalPages) {
	                currentPage = selectedPage;
	            } else {
	                System.out.println("없는 페이지입니다.");
	            }
	        } 
	        else{
	        	System.out.println(" ");
	        	System.out.println("[ 잘못된 입력입니다. 아래 형식대로 입력해 주세요. ]");
	        	System.out.println("1. 게시판 상세보기 : 엔터키를 입력하세요.");
	        	System.out.println("2. 페이지번호[1][2] : 숫자 입력하세요. ");
	        	System.out.println("3. 나가기 : x 입력하세요. ");
	        	
	        };
	        
	        //페이징번호 입력	
//	        int selectedPage=Integer.parseInt(input);
//	        try {
//	        	if(selectedPage>=1&&selectedPage<=totalPages) {
//		        	currentPage=selectedPage;
//		        }else {
//		        	System.out.println("없는 페이지입니다.");
//		        }
//	        }catch(NumberFormatException e) {
//	        	System.out.println("숫자를 입력해 주세요.");
//	        	
//	        }
	        
		}
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
		read();
		mainMenu();
	}
	
	//댓글쓰기
	public void addComment(int bno) {
		 System.out.println("[댓글 작성]");
		 System.out.print("내용: ");
		 String content = scanner.nextLine();
		 if(loginUserId==null) {
			 System.out.println("로그인 후 댓글을 작성할 수 있습니다.");
			 return;
		 }
		 String menuNo = printSubMenu();
		 if(!menuNo.equals("1")) return;
		 try {
			 String sql="""
			 		INSERT INTO comments(cno,bno,userId,content,cdate)
			 		VALUES(seq_cno.NEXTVAL,?,?,?,SYSDATE)
			 		""";
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 pstmt.setInt(1, bno);
			 pstmt.setString(2, loginUserId);
			 pstmt.setString(3, content);
			 pstmt.executeUpdate();
			 pstmt.close();
			 System.out.println("댓글이 등록 되었습니다.");
			 read();
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
	}
	
	public void listComment(int bno) {
        System.out.println("-------------------------------------------------------------------------------------");

		System.out.println("[댓글 목록]");
	    //System.out.println("----------------------------------------");
	    try {
	    	String sql="""
	    			SELECT cno,bno,userId,content,cdate FROM comments
	    			WHERE bno=?
	    			ORDER BY cno
	    			""";
	    	PreparedStatement pstmt = conn.prepareStatement(sql);
	    	pstmt.setInt(1,bno);
	    	ResultSet rs=pstmt.executeQuery();
	    	while(rs.next()) {
	    		System.out.printf("댓글번호:%d | 작성자:%s | 날짜: %s\n",rs.getInt("cno"),rs.getString("userId"),rs.getDate("cdate"));
	    		System.out.printf("댓글내용:%s\n", rs.getString("content"));
	    		//System.out.println("----------------------------------------");
	    	}
	    	
	    	rs.close();
	    	pstmt.close();
	    	
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	    
	}
	
	//로그인
	public void login() {
		System.out.println("[로그인]");
		System.out.println("아이디: ");
		String inputId=scanner.nextLine();
		System.out.println("비밀번호: ");
		String inputPassword=scanner.nextLine();
		//보조메뉴
		String menuNo=printSubMenu();
		if(menuNo.equals("1")) {
			try {
			String sql="""
					SELECT userId,username,userpassword,userage,usermail FROM users WHERE userId=?
					""";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,inputId);
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()) {
				String dbPassword=rs.getString("userpassword");
				if(BCrypt.checkpw(inputPassword, dbPassword)) {
					loginUserId=rs.getString("userId");
					System.out.println("˙ ͜ʟ˙ "+loginUserId+"님 환영합니다.");
				}else {
					System.out.println("비밀버호가 일치하지 않습니다.");
				}
				
			}
			else{
				System.out.println("✎⁾⁾⁾ 아이디가 존재하지 않습니다.");
				System.out.println("[새사용자 정보]");
				System.out.println("아이디: ");
				inputId=scanner.nextLine();
			}
			rs.close();
			pstmt.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
		mainMenu();
	}
	
	public void logout() {
		loginUserId=null;
	}
	
	public void join() {
		System.out.println("[새 사용자 입력]");
		System.out.println("아이디:");
		String joinId=scanner.nextLine();
		System.out.println("이름:");
		String joinName=scanner.nextLine();
		System.out.println("비밀번호:");
		String joinPassword=scanner.nextLine();
		System.out.println("나이:");
		String joinAge=scanner.nextLine();
		System.out.println("이메일:");
		String joinMail=scanner.nextLine();
		//보조메뉴
		String menuNo=printSubMenu();
		if(menuNo.equals("1")) {
		try {
			//비밀번호해시처리
			String hashedPassword=BCrypt.hashpw(joinPassword,BCrypt.gensalt());
			String sql="""
					INSERT INTO users(userid,username,userpassword,userage,usermail) 
					VALUES(?,?,?,?,?)
					""";
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1,joinId);
			pstmt.setString(2,joinName);
			pstmt.setString(3,hashedPassword);
			pstmt.setString(4,joinAge);
			pstmt.setString(5,joinMail);
			int rows=pstmt.executeUpdate();
			pstmt.close();
			
		}catch(Exception e) {
			e.printStackTrace();
			exit();
		}
		System.out.println("회원가입이 완료되었습니다.");
		}
		
		mainMenu();
	}
	
	//게시물 생성
	public void create() {
		Board board = new Board();
		if(loginUserId==null) {
			System.out.println("˙ ͜ʟ˙ 로그인 후 글을 쓸 수 있습니다.");
			login();
			return;
		}
		System.out.println("[새 게시물 입력]");
		System.out.println("제목: ");
		//set
		board.setBtitle(scanner.nextLine());
		System.out.println("내용: ");
		board.setBcontent(scanner.nextLine());
		
		board.setBwriter(loginUserId);
//		System.out.println("like:");
//		board.setBlike(Integer.parseInt(scanner.nextLine()));
		
		System.out.println("첨부파일 경로 입력,없으면 엔터 (예: C:/Temp/snow.jpg): ");
	    String filePath = scanner.nextLine();
	    File file = new File(filePath);
	    boolean hasFile = file.exists() && file.isFile();

	    if (hasFile) {
	        board.setBfilename(file.getName());
	    } else {
	        board.setBfilename(null); // 파일 없으면 null
	    }
		
		//보조메뉴
		String menuNo=printSubMenu();
		if(menuNo.equals("1")) {
			try {
				String sql="""
						INSERT INTO CINEMABOARDS(bno,btitle,bcontent,bwriter,bdate,bfilename, bfiledata)
						VALUES(SEQ_BNO.NEXTVAL,?,?,?,SYSDATE,?,?)
						""";
				PreparedStatement pstmt=conn.prepareStatement(sql,new String[] {"bno"});
				pstmt.setString(1, board.getBtitle());
				pstmt.setString(2, board.getBcontent());
				pstmt.setString(3, loginUserId);
				//pstmt.setInt(4, board.getBlike());
				// 파일명과 데이터
	            if (hasFile) {
	                pstmt.setString(4, board.getBfilename());
	                FileInputStream fis = new FileInputStream(file);
	                pstmt.setBlob(5, fis);
	                fis.close();
	            } else {
	                pstmt.setString(4, null);
	                pstmt.setBlob(5, (java.io.InputStream) null);
	            }

	            int rows = pstmt.executeUpdate();
	            if (rows == 1) {
	                System.out.println("게시물 등록 성공");
	            }
	            pstmt.close();

	        }catch(Exception e) {
				e.printStackTrace();
				exit();
			}
		}
		
		pagingList();
	}
	
	//게시물 상세
	public void read() {
        System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("[게시물 읽기] 게시물번호를 입력해 주세요.");
        System.out.println("-------------------------------------------------------------------------------------");

		//list();
		System.out.println("게시물 번호 : ");
		int bno=Integer.parseInt(scanner.nextLine());
		try {
			String sql="""
					SELECT bno,btitle,bcontent,bwriter,bdate,blike,bfilename,bfiledata
					FROM CINEMABOARDS
					WHERE bno=?
					""";
			PreparedStatement pstmt= conn.prepareStatement(sql);
			pstmt.setInt(1, bno);//bno=Integer.parseInt(scanner.nextLine());
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()) {
				Board board=new Board();
				board.setBno(rs.getInt("bno"));
				board.setBtitle(rs.getString("btitle"));
				board.setBcontent(rs.getString("bcontent"));
				board.setBwriter(rs.getString("bwriter"));
				board.setBdate(rs.getDate("bdate"));
				board.setBlike(rs.getInt("blike"));
				String filename = safeString(rs.getString("bfilename"));
				board.setBfilename(filename);
				
				System.out.println("-------------------------------------------------------------------------------------");
				System.out.println("번호: "+board.getBno());
				System.out.println("제목: "+board.getBtitle());
				System.out.println("내용: "+board.getBcontent());
				
				System.out.println("작성자: "+board.getBwriter());
				System.out.println("날짜: "+board.getBdate());
				System.out.println("좋아요: "+board.getBlike());
				System.out.println("첨부파일: "+(filename != null ? filename : "없음"));
				// 파일 다운로드 처리
	            if (filename != null) {
	                Blob fileData = rs.getBlob("bfiledata");
	                if (fileData != null) {
	                    InputStream is = fileData.getBinaryStream();
	                    File outFile = new File("C:/Temp/downloaded_" + filename);
	                    FileOutputStream fos = new FileOutputStream(outFile);
	                    byte[] buffer = new byte[1024];
	                    int bytesRead;
	                    while ((bytesRead = is.read(buffer)) != -1) {
	                        fos.write(buffer, 0, bytesRead);
	                    }
	                    fos.close();
	                    is.close();
	                    System.out.println("첨부파일 다운로드 완료: " + outFile.getAbsolutePath());
	                }
	            } 
	            System.out.println("댓글목록 : ");
	            listComment(bno);
	            //보조메뉴 출력
		        System.out.println("-------------------------------------------------------------------------------------");
	            System.out.println("게시글 관리: 1.수정 | 2.삭제 | 3.좋아요 |  4.댓글 | 5.목록 | 6.홈 ");
	            System.out.println("메뉴선택: ");
	            String menuNo=scanner.nextLine();
	            
	            if(menuNo.equals("1")) {
	            	update(board);
	            }else if(menuNo.equals("2")) {
	            	delete(board);
	            }else if(menuNo.equals("3")) {
	            	like(board);
	            }else if(menuNo.equals("4")) {
	            	addComment(bno);
	            }else if(menuNo.equals("5")) {
	            	pagingList();     
	            }
	            else if(menuNo.equals("6")) {
	            	mainMenu();  	
	            }
			}
				rs.close();
				pstmt.close();
					
		}catch(Exception e) {
			e.printStackTrace();
		}
		mainMenu();
	}
	
	public void listComments(int bno) {
		System.out.println("[댓글 목록]");
        System.out.println("-------------------------------------------------------------------------------------");
	}
	
	//좋아요
	public void like(Board board) {
		System.out.println("[좋아요]");
//		board.setBlike(scanner.nextInt());
//		scanner.nextLine();
		String menuNo=printSubMenu();
		
		if(menuNo.equals("1")) {
			 try {
		            // 중복 좋아요 방지용 조회
		            String checkSql = "SELECT * FROM likes WHERE userId = ? AND bno = ?";
		            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
		            checkStmt.setString(1, loginUserId);
		            checkStmt.setInt(2, board.getBno());
		            ResultSet rs = checkStmt.executeQuery();

		            if (rs.next()) {
		                System.out.println("이미 좋아요를 누른 게시물입니다.");
		                rs.close();
		                checkStmt.close();
		                return;
		            }
		            rs.close();
		            checkStmt.close();

		            // 좋아요 등록 → likes 테이블에 insert
		            String insertSql = """
		                INSERT INTO likes(userId, bno, blike)
		                VALUES (?, ?, 1)
		            """;
		            PreparedStatement pstmt = conn.prepareStatement(insertSql);
		            pstmt.setString(1, loginUserId);
		            pstmt.setInt(2, board.getBno());
		            pstmt.executeUpdate();
		            pstmt.close();

		            System.out.println("좋아요가 등록되었습니다.");

		        } catch(Exception e) {
		            e.printStackTrace();
		            try { conn.rollback(); } catch (SQLException se) {}
		            exit();
		        }
		}
		list();		
	}
	
	//조회
	public void search() {
		while(true) {
			//System.out.println("[조회하기]");
	        System.out.println("-------------------------------------------------------------------------------------");
	        System.out.println("1.좋아요 조회 | 2.댓글 조회 | 3.검색어로 조회 | 4.게시물번호로 조회 | 5.나가기");
	        System.out.println("-------------------------------------------------------------------------------------");

	        System.out.println("[메뉴선택] : ");
	        String menuNo=scanner.nextLine();
	        switch(menuNo) {
		        case "1"->{
		        	System.out.println("조회 아이디: ");
		    		String userId=scanner.nextLine();
		    		try {
		    			String sql="""
		    					SELECT u.userId,u.username,c.btitle,c.bcontent,c.blike,c.bdate
		    					FROM likes l
		    					JOIN users u ON u.userId=l.userId
		    					JOIN cinemaboards c ON c.bno=l.bno
		    					WHERE u.userId=?
		    					ORDER BY c.bdate DESC
		    					""";
		    			PreparedStatement pstmt = conn.prepareStatement(sql);
		    			pstmt.setString(1, userId);
		    			ResultSet rs=pstmt.executeQuery();
		    			//테이블헤드
		    			System.out.printf("%-12s%-10s%-16s%-20s%-8s%-20s \n","ID","이름","제목","게시글","좋아요","날짜");
		    			 System.out.println("---------------------------------------------------------------------------------------------------------------------");
		    			boolean hasResults=false;
		    			while(rs.next()) {
		    			hasResults=true;
		    	       
		    	      //글자 ... 처리
		    			String bcontent = rs.getString("bcontent");
		    			if (bcontent.length() > 20) {
		    			    bcontent = bcontent.substring(0, 17) + "...";
		    			}
		    			
		    	        System.out.printf("%-12s%-10s%-16s%-20s%-8d%-20s \n",
		    				rs.getString("userId"),
		    				rs.getString("username"),
		    				rs.getString("btitle"),
		    				rs.getString("bcontent"),
		    				rs.getInt("blike"),
		    				rs.getDate("bdate")
		    				.toString()
		    				);
		    			}
		    			if(!hasResults) {
		    				System.out.println("::::: 해당 사용자의 좋아요 내역이 없습니다. ::::: ");
		    			}
		    			rs.close();
		    			pstmt.close();
		    			
			    		}catch(Exception e) {
			    			e.printStackTrace();
			    		}
		        	}
			        case "2"->{
			        	
			        	System.out.println("게시글 작성자 아이디: ");
			    		String userId=scanner.nextLine();
			    		try {
			    			String sql="""
			    					SELECT u.userId AS userId,u.username AS username,c.btitle AS btitle,c.bcontent AS bcontent,cm.content AS content 
			    					FROM comments cm
									JOIN users u ON u.userId=cm.userId
									JOIN CINEMABOARDS c ON c.bno=cm.bno
									WHERE u.userId=?
									ORDER BY u.userId DESC
			    					""";
			    			PreparedStatement pstmt = conn.prepareStatement(sql);
			    			pstmt.setString(1, userId);
			    			ResultSet rs=pstmt.executeQuery();
			    			//테이블헤드
			    			System.out.printf("%-10s%-10s%-20s%-30s%-20s \n","ID","이름","제목","게시글","댓글");
			    	        System.out.println("-----------------------------------------------------------------------------------------------");

			    			boolean hasResults=false;
			    			while(rs.next()) {
			    			hasResults=true;
			    	        
		    			    
			    			//글자 ... 처리
			    			String bcontent = rs.getString("bcontent");
			    			if (bcontent.length() > 20) {
			    			    bcontent = bcontent.substring(0, 17) + "...";
			    			}
			    			String content = rs.getString("content");
			    			if (content.length() > 20) {
			    			    content = content.substring(0, 17) + "...";
			    			}
			    			System.out.printf("%-10s%-10s%-20s%-30s%-20s \n",
			    				rs.getString("userId"),
			    				rs.getString("username"),
			    				rs.getString("btitle"),
			    				bcontent,
			    				content
			    				
			    				);
			    			
			    			System.out.println();
			    			}
			    			if(!hasResults) {
			    				System.out.println("::::: 해당 사용자의 내역이 없습니다. ::::: ");
			    			}
			    			rs.close();
			    			pstmt.close();
			    			
				    		}catch(Exception e) {
				    			e.printStackTrace();
				    		}
			    		
			        }
			        case "3"->{
			        	System.out.println("검색어: ");
			        	String keyword=scanner.nextLine();
			        	
			        	try {
			        		String sql="""
			        				SELECT bno,btitle,bcontent,bwriter,bdate FROM CINEMABOARDS
			        				WHERE bcontent LIKE ?
			        				ORDER BY bdate DESC 
			        				""";
			        		PreparedStatement pstmt=conn.prepareStatement(sql);
			        		pstmt.setString(1,"%"+keyword+"%");
			        		ResultSet rs=pstmt.executeQuery();
			        		
			        		System.out.printf("%-6s%-20s%-40s%-14s%-20s \n","No.","제목","게시글","작성자","작성일");
			    	        System.out.println("------------------------------------------------------------------------------------------------------------");

			        		
			        		//글이 길면 ...처리
			        		boolean hasResults=false;
			        		while(rs.next()) {
			        			hasResults=true;
			        		String bcontent = rs.getString("bcontent");
			        		if(bcontent.length()>20) {
			        			bcontent=bcontent.substring(0,17)+"...";
			        		}
			        		
			        		System.out.printf("%-6d%-20s%-40s%-14s%-20s\n",
			        				rs.getInt("bno"),
			        				rs.getString("btitle"),
			        				bcontent,
			        				rs.getString("bwriter"),
			        				rs.getTimestamp("bdate").toString()
			        				);
			        		}//while
			        		if(!hasResults) {
			        			System.out.println("::::: 검색된 게시물이 없습니다. :::::");
			        		}
			        		rs.close();
			        		pstmt.close();
			        	
			        	}catch(Exception e) {
			        		e.printStackTrace();
			        	}
			        	
			        	read();
			        }
			        case "4"->{
			        	list();
			        	read();
			        }
			        case "5"->{
			        	System.out.println("메인메뉴");
			        	mainMenu();
			        }
			        default->{
			        	System.out.println("1~5번호를 입력해 주세요.");
			        }
	        
	            }//switch
	
		}//while
	}
	//페이징 없는 리스트
	public void list() {
		System.out.println();
		System.out.println("[게시물 목록] "+"로그인: "+(loginUserId!=null?loginUserId:"로그인 안됨"));
        System.out.println("-------------------------------------------------------------------------------------");
		System.out.printf("%-6s%-30s%-16s%-16s%-6s%-20s \n","no","제목","작성자","등록일","좋아요","첨부파일");
        System.out.println("-------------------------------------------------------------------------------------");
		
		try {
			String sql="""
					SELECT bno,btitle,bwriter,bdate,blike,bfilename FROM CINEMABOARDS
					ORDER BY bno DESC
					""";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()) {
				Board board=new Board();
				board.setBno(rs.getInt("bno"));
				board.setBtitle(rs.getString("btitle"));
				board.setBwriter(rs.getString("bwriter"));
				board.setBdate(rs.getDate("bdate"));
				board.setBlike(rs.getInt("blike"));
				//safeString(board.setBfilename(rs.getString("bfilename")));
				String filename=safeString(rs.getString("bfilename"));
				board.setBfilename(filename);
				//가져온내용 찍어보기,set된걸 get으로 보여줌
				System.out.printf("%-6s%-30s%-16s%-16s%-6s%-20s \n",
				board.getBno(),
				board.getBtitle(),
				board.getBwriter(),
				board.getBdate(),
				board.getBlike(),
				board.getBfilename()
				);
			}
			rs.close();
			pstmt.close();
		}catch(SQLException e) {
			e.printStackTrace();
			exit();
		}catch(Exception e) {
			e.printStackTrace();
			exit();
		}
		
	}
	
	//게시물 수정
	public void update(Board board) {
		System.out.println("[수정 내용 입력]");
		System.out.print("제목:");
		board.setBtitle(scanner.nextLine());
		System.out.print("내용:");
		board.setBcontent(scanner.nextLine());
		System.out.print("작성자:");
		board.setBwriter(scanner.nextLine());
		//보조메뉴
		String menuNo=printSubMenu();
		if(menuNo.equals("1")) {
			try {
				String sql="""
						UPDATE cinemaboards SET btitle=?,bcontent=?,bwriter=?
						WHERE bno=?
						""";
				PreparedStatement pstmt=conn.prepareStatement(sql);
				pstmt.setString(1,board.getBtitle());
				pstmt.setString(2,board.getBcontent());
				pstmt.setString(3,board.getBwriter());
				pstmt.setInt(4,board.getBno());
				pstmt.executeUpdate();
				pstmt.close();
			}
			catch(Exception e) {
				e.printStackTrace();
				exit();
			}
		}
		list();
	}
	
	//게시물 삭제
	public void delete(Board board) {
		try {
			String sql="DELETE FROM cinemaboards WHERE bno=? "; 
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setInt(1,board.getBno());
			pstmt.executeUpdate();
			pstmt.close();
		} catch(Exception e) {
			e.printStackTrace();
			exit();
		}
	}
	
	//게시물 전체 삭제
	private void clear() {
		
		System.out.println("[게시물 전체 삭제]");
        System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("보조메뉴: 1.Ok | 2.Cancle ");
		System.out.print("메뉴선택: ");
		String menuNo=scanner.nextLine();
		if(menuNo.equals("1")) {
			try {
				String sql="""
						TRUNCATE TABLE cinemaboards
						""";
				PreparedStatement pstmt=conn.prepareStatement(sql);
				pstmt.execute();
				pstmt.close();
			}catch(Exception e) {
				e.printStackTrace();
				exit();
			}
		}
		list();
	}

	private void exit() {
		if(conn!=null) {
			try {
				conn.close();
				}catch(SQLException e) {
					e.printStackTrace();
				}
		}
		System.out.println("게시판종료");
		System.exit(0);

	}
	public static void main(String[] args) {
		BoardEx boardEx= new BoardEx();
		boardEx.mainMenu();
		//boardEx.list();
		
	}
}
