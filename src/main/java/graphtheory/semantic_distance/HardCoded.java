package graphtheory.semantic_distance;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The return valued of sd function is hard coded and stored in database.
 * We here read them from database and store in matrix.
 */
public class HardCoded extends SD{
    double[][]a;
    public HardCoded(Connection conn, int n) throws SQLException {
        System.out.println("Reading hard code...");
        a=new double[n][n];
        PreparedStatement pstmt=conn.prepareStatement("select * from hardcode", ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(Integer.MIN_VALUE);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ResultSet ret=pstmt.executeQuery();
        while(ret.next()){
            int x=ret.getInt("x");
            int y=ret.getInt("y");
            a[x][y]=a[y][x]=ret.getDouble("v");
        }
        conn.commit();
        ret.close();
        pstmt.close();
    }

    @Override
    public double cal(int i, int j) {
        return a[i][j];
    }
}
