/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */
import java.math.*;
import java.nio.charset.*;
import java.security.*;
import java.sql.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;


/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}

	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 *
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 *
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 * obtains the metadata object for the returned result set.  The metadata
		 * contains row and column info.
		*/
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>();
		while (rs.next()){
			List<String> record = new ArrayList<String>();
			for (int i=1; i<=numCol; ++i)
				record.add(rs.getString (i));
			result.add(record);
		}//end while
		stmt.close ();
		return result;
	}//end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current
	 * value of sequence used for autogenerated keys
	 *
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */

	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();

		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 *
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if

		Ticketmaster esql = null;

		try{
			System.out.println("(1)");

			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}

			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];



			esql = new Ticketmaster (dbname, dbport, user, "");

			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");

				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static byte[] getSHA(final String input) throws NoSuchAlgorithmException {

		final MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	}

	public static String toHexString(final byte[] hash) {

		final BigInteger number = new BigInteger(1, hash);

		final StringBuilder hexString = new StringBuilder(number.toString(16));

		while (hexString.length() < 32) {
			hexString.insert(0, '0');
		}

		return hexString.toString();
	}

	public static void AddUser(Ticketmaster esql){//1
		String userEmail = "";
		String userFname = "";
		String userLname = "";
		String userPass = "";
		String userPhoneString = "";
		long userPhone = 0;

		System.out.println("Please enter first name: ");
		try {
			userFname = in.readLine();
			if (userFname.length() == 0) {
				System.out.println("Must enter first name");
				return;
			}
			if (userFname.length() > 32) {
				System.out.println("First name must be less than 32 characters");
				return;
			}
		} catch (final Exception e) {
			System.out.print(e);
			return;
		}

		System.out.println("Please enter last name: ");
		try {
			userLname = in.readLine();
			if (userLname.length() == 0) {
				System.out.println("Must enter last name");
				return;
			}
			if (userLname.length() > 32) {
				System.out.println("Last name must be less than 32 characters");
				return;
			}
		} catch (final Exception e) {
			System.out.print(e);
			return;
		}

		System.out.println("Please enter phone number: ");
		try {
			userPhoneString = in.readLine();
			if (userPhoneString.length() != 10) {
				System.out.println("Phone number must have 10 digits");
				return;
			}
			userPhone = Long.parseLong(userPhoneString);
		} catch (final Exception e) {
			System.out.print(e);
			return;
		}

		System.out.println("Please enter email: ");
		try {
			userEmail = in.readLine();
			if (userEmail.length() == 0) {
				System.out.println("Must enter email");
				return;
			}
			if (userEmail.length() > 64) {
				System.out.println("Email must be less than 64 characters");
				return;
			}
		} catch (final Exception e) {
			System.out.print(e);
			return;
		}

		System.out.println("Please enter password: ");
		try {
			userPass = in.readLine();
		} catch (final Exception e) {
			System.out.print(e);
			return;
		}
		// generate hash of password
		try {
			userPass = toHexString(getSHA(userPass));
		} catch (final Exception e) {
			System.out.print(e);
			return;
		}

		try {
			final PreparedStatement stmt = esql._connection
					.prepareStatement("INSERT INTO Users(email, lname, fname, phone, pwd) VALUES (?, ?, ?, ?, ?)");

			stmt.setString(1, userEmail);
			stmt.setString(2, userLname);
			stmt.setString(3, userFname);
			stmt.setLong(4, userPhone);
			stmt.setString(5, userPass);
			stmt.executeUpdate();
		} catch (final Exception e) {
			System.out.println(e);
			return;
		}

	}

	public static void AddBooking(Ticketmaster esql){//2
		String email = "";
		String status = "";
		String bdatetime = "";
		int bid = 0;
		String seats = "";
		String sid = "";

		List<List<String>> temp = null;

		try {
			//Get user email
			System.out.println("Please enter user email:");
			email = in.readLine();
			if (email.length() == 0){
				System.out.println("Must enter an email");
				return;
			} else if (email.length() > 64){
				System.out.println("Email must be under 64 characters");
				return;
			}

			// //Get new booking ID
			temp = esql.executeQueryAndReturnResult("SELECT max(B.bid) FROM Bookings B");
			bid = Integer.parseInt(temp.get(0).get(0));
			bid++;

			//Get status of booking
			System.out.println("Please enter status of Booking");
			status = in.readLine();
			if (status.length() == 0){
				System.out.println("Must enter status");
				return;
			} else if (status.length() > 16){
				System.out.println("Status must be less than 16 characters");
				return;
			}

			//Get date and time of booking
			System.out.println("Please enter Date and time of booking in format '2/5/2016 4:06'");
			bdatetime = in.readLine();
			if (bdatetime.length() == 0){
				System.out.println("Must enter date and time");
				return;
			}else if (bdatetime.length() > 16){
				System.out.println("Must follow above format");
				return;
			}


			//Get number of seats
			System.out.println("Please enter number of seats for booking");
			seats = in.readLine();
			if (seats.length() == 0 || seats == "0"){
				System.out.println("Must enter seats greater than 0");
			}

			//Get sid
			System.out.println("Please enter SID");
			sid = in.readLine();
			if (sid.length() == 0){
				System.out.println("Must enter sid");
				return;
			}


		} catch (Exception e) {
			System.out.println(e);
			return;
		}
		String stmt = "INSERT INTO Bookings(bid, status, bdatetime, seats, sid, email) VALUES (" + bid + "," + "'" + status + "'" + "," + "'" + bdatetime + "'" + "," + seats + "," + sid + "," + "'" + email + "'" + ")";
		try {
			esql.executeUpdate(stmt);
		} catch (Exception e) {
			System.out.println(e);
			return;
		}

	}

	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		String stmt = "INSERT INTO Movies(mvid, title, rdate, country, description, duration, lang, genre) VALUES(";
		int mvid = 0;
		int duration = 0;
		int sid = 0;
		int tid = 0;
		String title = "";
		String date = "";
		String country = "";
		String description = "";
		String language = "";
		String genre = "";
		String sDate = "";
		String sTime = "";
		String eTime = "";
		List<List<String>> temp = null;

		try {
			//get next movie ID
			temp = esql.executeQueryAndReturnResult("SELECT max(M.mvid) FROM Movies M");
			mvid = Integer.parseInt(temp.get(0).get(0));
			mvid++;

			System.out.println("Please enter Movie title");
			title = in.readLine();
			if (title.length() == 0){
				System.out.println("Must enter movie title");
				return;
			} else if (title.length() > 128){
				System.out.println("Title must be less than 128 characters");
				return;
			}

			System.out.println("Please enter movie release date");
			date = in.readLine();
			if (date.length() == 0){
				System.out.println("Must enter date");
				return;
			}

			System.out.println("Please enter the movie's release country");
			country = in.readLine();
			if (country.length() == 0){
				System.out.println("Must enter release country");
				return;
			} else if (country.length() > 64){
				System.out.println("Country must be less than 64 characters");
				return;
			}

			System.out.println("Please enter movie description");
			description = in.readLine();

			System.out.println("Please enter movie duration in seconds");
			duration = Integer.parseInt(in.readLine());

			System.out.println("Please enter the movie language code");
			language = in.readLine();
			if (language.length() != 2){
				System.out.println("Language code must be 2 characters");
				return;
			}

			//get new show ID
			temp = esql.executeQueryAndReturnResult("SELECT max(B.bid) FROM Bookings B");
			sid = Integer.parseInt(temp.get(0).get(0));
			sid++;

			System.out.println("Please enter the show date");
			sDate = in.readLine();

			System.out.println("Please enter the show start time");
			sTime = in.readLine();

			System.out.println("Please enter the show end time");
			eTime = in.readLine();

			System.out.println("Please enter the theater ID");
			tid = Integer.parseInt(in.readLine());

			stmt += mvid + ", '" + title + "','" + date + "', '" + country + "', '" + description + "', " + duration + ", '" + language + "', '" + genre + "')";
			esql.executeUpdate(stmt);

			stmt = "INSERT INTO Shows(sid, mvid, sdate, sttime, edtime) VALUES(";
			stmt += sid + ", " + mvid + ", '" + sDate + "', '" + sTime + "', '" + eTime + "')";
			esql.executeUpdate(stmt);

			stmt = "INSERT INTO Plays(sid, tid) VALUES(";
			stmt += sid + ", " + tid + ")";
			esql.executeUpdate(stmt);

		} catch (Exception e){
			System.out.println(e);
			return;
		}


	}

	public static void CancelPendingBookings(Ticketmaster esql){//4
		try {
			String pstatus = "'Pending'";
			String query = String.format("DELETE FROM Bookings WHERE status = %s", pstatus);
			// here is the sql statement in the () above
			// DELETE FROM Booking
			// WHERE status = %s
			//,pstatus)
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		try{
			System.out.print("Pleaser enter the booking ID with the seating you wish to change: ");
				String bID = in.readLine();
			System.out.print("Please enter the seat ID of the seat that you want to change: ");
				String oldSeat = in.readLine();
			System.out.print("Please enter the seat ID of the new seat that you want to change to: ");
				String newSeat = in.readLine();
			String query = String.format("UPDATE ShowSeats SET ssid= %s WHERE ssid  = %s AND bid = bID", newSeat, oldSeat, bID);
				// here is the sql statement in the () above
				// "UPDATE ShowSeats
				// SET ssid = %s
				// WHERE ssid  = %s
				// AND bid = bID
				// , newSeat, oldSeat, bID)
			int count = esql.executeQueryAndPrintResult(query);
			}catch(Exception e) {
				System.err.println(e.getMessage());
			}

		//Because the current implementation of this function doesn't check if the seats are unique, I tried to come up with another solution:
		// UPDATE ShowSeats
		// SET S1.ssid = S2.ssid <= potential error?
		// FROM ShowSeats S1, ShowSeats S2
		// WHERE S1.ssid = %s AND S2.ssid = %s
		// AND S1.bid = %s AND S1.price = S2.price
		// AND UNIQUE(S1.sid, S2.ssid)
		// , oldSeat, newSeat, bID)
		// However, I am less certain this implementation will work as I don't know if line 2(marked with arrow)is valid.
	}

	public static void RemovePayment(Ticketmaster esql){//6
		try{
			String canc = "'cancelled'";
			System.out.print("Please enter the bookingID of the booking to remove the payment from: ");
			String bID = in.readLine();

			String query = String.format("DELETE FROM Payments WHERE bid = %s", bID);
			// here is the sql statement in the () above
			// DELETE FROM Payment
			// WHERE bookingID = %s
			// ,bID)
			int count = esql.executeQueryAndPrintResult(query);
			System.out.println("total amount of payments removed: " + count);

			String query2 = String.format("UPDATE Bookings SET status = %s WHERE bid = %s", canc,bID);
			// here is the sql statement in the () above
			// UPDATE Booking
			// SET status = %s
			// WHERE bookingID = %s
			// ,canc,bID)
			count = esql.executeQueryAndPrintResult(query2);
			System.out.println("total amount of bookings cancelled: " + count);

		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ClearCancelledBookings(Ticketmaster esql){//7
		try {
			String cstatus = "'Cancelled'";
			String query = String.format("DELETE FROM Bookings WHERE status = %s", cstatus);
			// here is the sql statement in the () above
			// DELETE FROM Booking
			// WHERE status = %s
			// ,cstatus)
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		try {
			System.out.print("Please enter the date to remove the shows: ");
			String uDate = in.readLine();
			String query = String.format("DELETE FROM Shows WHERE sdate = '%s'", uDate);
			// here is the sql statement in the () above
			// DELETE Show
			// WHERE date = %s
			//,uDate)
			int count = esql.executeQueryAndPrintResult(query);
			System.out.println("total amount of shows removed: " + count);
			System.out.print("Shows have been cancelled, please remove the payments for that date using operation 4. ");
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		try {
			System.out.print("Please enter in the cinema ID: ");
			String cinID = in.readLine();
			System.out.print("Please enter in show ID: ");
			String showID = in.readLine();
			String query = String.format("SELECT T.tname FROM Plays P, Theaters T, Cinemas C, Shows S WHERE T.cid = C.cid AND C.cid = %s AND S.sid = %s AND P.sid = S.sid AND P.tid = T.tid", cinID, showID);
			// here is the sql statement in the () above
			// SELECT T.theaterName
			// FROM played_in P, Cinema_Theather T, Cinema C, Show S
			// WHERE T.cinemaID = C.cinemaID
			// AND C.cinemaID = %s
			// AND S.showID = %s
			// AND P.showID = S.showID
			// AND P.cinemaTheatherID = T.cinemaTheatherID
			//,cinID, showID);
			int count = esql.executeQueryAndPrintResult(query);
			System.out.println("total amount of theaters: " + count);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		try{
			System.out.print("Please enter in the starting time: ");
			String stime = in.readLine();
			System.out.print("Please enter in the date: ");
			String sdate = in.readLine();
			String query = String.format("SELECT S.sid FROM Shows S WHERE S.sttime = '%s' AND S.sdate = '%s'", stime, sdate);
			// here is the sql statement in the () above
			// SELECT S.showID
			// FROM Show S
			// WHERE S.startTime = %s
			// AND S.date = %s
			//,stime,sdate);
			int count = esql.executeQueryAndPrintResult(query);
			System.out.println("total amount of shows: " + count);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		String stmt = "SELECT title FROM movies WHERE title LIKE '%Love%' AND rdate > '12/31/2010'";
		try {
			esql.executeQueryAndPrintResult(stmt);
		} catch (Exception e){
			System.out.println(e);
			return;
		}
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		String stmt = "SELECT U.fname, U.lname, U.email FROM users U, bookings B WHERE B.STATUS = 'Pending' AND B.email = U.email;";

		try {
			esql.executeQueryAndPrintResult(stmt);
		} catch (Exception e) {
			System.out.println(e);
			return;
		}

	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		String stmt = "SELECT M.title, M.duration, S.sdate, S.sttime FROM movies M, shows S, cinemas C, theaters T, plays P WHERE M.title = '";
		String title = "";
		int cid = 0;
		String startDate = "";
		String endDate = "";

		try {
			System.out.println("Please enter the Movie Title");
			title = in.readLine();

			System.out.println("Please enter cinema ID");
			cid = Integer.parseInt(in.readLine());

			System.out.println("Please enter the start date in this format: dd/mm/yyyy");
			startDate = in.readLine();

			System.out.println("Please enter the end date in this format: dd/mm/yyyy");
			endDate = in.readLine();

			stmt += title + "' AND C.cid = " + cid + " AND C.cid = T.cid AND T.tid = P.tid AND S.sid = P.sid AND M.mvid = S.mvid AND S.sdate >= '" + startDate + "' AND S.sdate <= '" + endDate + "';";

			esql.executeQueryAndPrintResult(stmt);


		} catch(Exception e){
			System.out.println(e);
			return;
		}




	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		String stmt = "SELECT DISTINCT M.title, S.sdate, S.sttime, T.tname, C.sno FROM bookings B, movies M, shows S, theaters T, cinemaseats C, plays P WHERE B.email = '";
		String email = "";
		try {
			System.out.println("Please enter user's email:");
			email = in.readLine();

			stmt += email + "' AND S.sid = B.bid AND P.tid = T.tid AND S.sid = P.sid;";
			esql.executeQueryAndPrintResult(stmt);
		} catch (Exception e){
			System.out.println(e);
			return;
		}
	}

}
