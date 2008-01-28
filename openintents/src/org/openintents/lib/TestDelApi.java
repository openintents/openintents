package org.openintents.lib;

public class TestDelApi{

	public static void main(String[] args) throws Exception
	{
		String[] res;
		DeliciousApiHelper dah=new DeliciousApiHelper(DeliciousApiHelper.MAGNOLIA_API,"zero.","0rch1d");
		
		res=dah.getTags();
		for (int i=0 ;i<res.length ;i++ )
		{
			System.out.println(res[i]);
		}

		dah.addPost(
			"http://www.html-world.de/program/http_8.php",
			"HTTP Response Codes",
			"a list of the most response codes.",
			new String[]{"code","mobile computing"},
			false
			);


	}
}
