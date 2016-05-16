package me.haolee.gp.serverside;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import me.haolee.gp.common.*;

class DatebaseQuery{
	/*
	 * 这个类的操作只有数据库查询，至于数据库的增删则放到DatabaseSync类里进行
	 * */
	private String dbName = null;
	private String dbUsername = null;
	private String dbPassword = null;
	private String dbURL = "jdbc:mysql://localhost:3306/";
	//VideoInfo?" + "user=root&password=MyNewPass4!&useSSL=false";
	
	//构造函数
	public DatebaseQuery() {
		this.dbName = Config.getValue("dbName", "VideoInfo");
		this.dbUsername = Config.getValue("dbUsername", "root");
		this.dbPassword = Config.getValue("dbPassword", "MyNewPass4!");
		this.dbURL = dbURL
				+this.dbName
				+"?user="+this.dbUsername
				+"&password="+this.dbPassword
				+"&useSSL=false";
	}
	
	/*
	 * 取出所有分类名称
	 * */
	public ArrayList<String> getCategoryList(int mode) {
		/*
		 * 数据库表名
		 * */
		String vodCategoryTable = "vodcategory";
		String liveCategoryTable = "livecategory";
		
		Connection connection = null;
		String sql = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		ArrayList<String> categoryList = new ArrayList<>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("成功加载MySQL驱动程序");
			connection = DriverManager.getConnection(dbURL);
			stmt = connection.createStatement();
			sql = "select * from ";
			if(mode == Command.MODE_VOD)
				sql += vodCategoryTable;
			else sql += liveCategoryTable;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				//System.out.println(resultSet.getString(1));
				categoryList.add(resultSet.getString("CategoryName"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(resultSet!=null)resultSet.close();
				if(stmt!=null)stmt.close();
				if(connection!=null)connection.close();
			} catch (SQLException e) {e.printStackTrace();}
		}//finally
		return categoryList;
	}

	/*
	 * 取出指定数量的视频记录
	 */
	public ArrayList<VideoInfo> getVideoSet(int mode, String category, 
							int videoDisplayStart,int videoDisplayStep) {
		/*
		 * 数据库表名
		 * */
		String vodTable = "vod";
		String liveTable = "live";
		
		Connection connection = null;
		String sql = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		ArrayList<VideoInfo> videoInfoList = new ArrayList<>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("成功加载MySQL驱动程序");
			connection = DriverManager.getConnection(dbURL);
			stmt = connection.createStatement();

			//SELECT * FROM vod WHERE Category="游戏" LIMIT 0,2
			//sql = "SELECT * FROM ";//拼接初始化
			if(mode == Command.MODE_VOD)
				sql = "SELECT vod.FileID,vod.Extension,vod.VideoName"
						+ ",vod.Duration,vod.Resolution,vod.CategoryName"
						+ ",vodcategory.CategoryRelativePath "
						+ "FROM vod INNER JOIN vodcategory "
						+ "ON vod.CategoryName = vodcategory.CategoryName"
						+ " WHERE vod.CategoryName="+"\""+category+"\" "
						+ "LIMIT "+videoDisplayStart+","+videoDisplayStep;
			else 
				sql = "SELECT live.FileID,live.Extension,live.VideoName"
						+ ",live.Duration,live.Resolution,live.CategoryName"
						+ ",livecategory.CategoryRelativePath "
						+ "FROM live INNER JOIN livecategory "
						+ "ON live.CategoryName = livecategory.CategoryName"
						+ " WHERE live.CategoryName="+"\""+category+"\" "
						+ "LIMIT "+videoDisplayStart+","+videoDisplayStep;
			
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				VideoInfo videoInfo = new VideoInfo(
						resultSet.getString("VideoName"),
						resultSet.getString("Duration"), 
						resultSet.getString("Resolution"),
						
						resultSet.getString("CategoryRelativePath")
						+resultSet.getString("FileID")
						+"."
						+resultSet.getString("Extension"));
				
				videoInfoList.add(videoInfo);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(resultSet!=null)resultSet.close();
				if(stmt!=null)stmt.close();
				if(connection!=null)connection.close();
			} catch (SQLException e) {e.printStackTrace();}
		}//finally
		return videoInfoList;//包含了videoDisplayStep个视频的详细信息，每个视频占一条
	}

	public int getTotalCount(int mode, String category) {
		
		/*
		 * 数据库表名
		 * */
		String vodTable = "vod";
		String liveTable = "live";
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		int recordCount = 0;
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("成功加载MySQL驱动程序");
			connection = DriverManager.getConnection(dbURL);
			stmt = connection.createStatement();
			
			if(mode == Command.MODE_VOD)
				resultSet = stmt.executeQuery("select count(*) from "
							+vodTable+" WHERE CategoryName="+"\""+category+"\" ");
			else
				resultSet = stmt.executeQuery("select count(*) from "
							+liveTable+" WHERE CategoryName="+"\""+category+"\" ");
			resultSet.next();
			recordCount = resultSet.getInt(1);
			
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			try {
				if(resultSet!=null)resultSet.close();
				if(stmt!=null)stmt.close();
				if(connection!=null)connection.close();
			} catch (SQLException e) {e.printStackTrace();}
		}//finally
		
		return recordCount;
	}
}
