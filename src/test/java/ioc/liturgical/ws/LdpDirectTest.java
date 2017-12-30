package ioc.liturgical.ws;

import java.util.GregorianCalendar;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.apache.tools.ant.types.resources.selectors.Date;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Suite;

import ioc.liturgical.ws.constants.LITURGICAL_CALENDAR_TYPE;
import ioc.liturgical.ws.ldp.LiturgicalDayProperties;

//import ioc.liturgical.test.framework.TestConstants;
//import ioc.liturgical.test.framework.TestUsers;
//import ioc.liturgical.ws.app.ServiceProvider;
//import ioc.liturgical.ws.constants.Constants;
//import ioc.liturgical.ws.constants.HTTP_RESPONSE_CODES;

public class LdpDirectTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	private static int daysInMonth(String month) {
		Integer monthAsInt = Integer.parseInt(month);
		if (monthAsInt == 2) {
			return 28; 
		} else if (monthAsInt == 4 || monthAsInt == 6 || monthAsInt == 9 || monthAsInt == 11) {
			return 30;
		} else {
			return 31;
		}
	}
	
	// // // //
	
	@Test
	/*
	 * NOTE: The default constructor creates a a LiturgicalDayProperties for
	 * the current date. 
	 */
	public void testLDPGregorianValidDefaultConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		LocalDate now = LocalDate.now(); // Java 8 replacement for Date
		
		String month = Integer.toString(now.getMonth().ordinal()+1);
		if (month.length() == 1)
			month = "0" + month;
		
		String day   = Integer.toString(now.getDayOfMonth());
		if (day.length() == 1)
			day = "0" + day;
		
		String year  = Integer.toString(now.getYear());
		
		LiturgicalDayProperties D = new LiturgicalDayProperties(year,month,day);

		assertEquals(D.getNbrMonth(),     today.getNbrMonth());	
		assertEquals(D.getNbrDayOfMonth(),today.getNbrDayOfMonth());
		assertEquals(D.getYear(),         today.getYear());
		
		assertEquals(D.getNbrMonth(),     month);	
		assertEquals(D.getNbrDayOfMonth(),day);
		assertEquals(D.getYear(),         now.getYear());		
	}
	
	@Test
	public void testLDPGregorianValidISOStringConstructor() {
		LiturgicalDayProperties theDate = new LiturgicalDayProperties("2016-11-19T12:00:00.000Z",
				                                                      LITURGICAL_CALENDAR_TYPE.GREGORIAN); 
		
		String month = "11";
		String day   = "19";
		String year  = "2016";
		
		LiturgicalDayProperties D     = new LiturgicalDayProperties(year,month,day);

		assertEquals(D.getNbrMonth(),     theDate.getNbrMonth());	
		assertEquals(D.getNbrDayOfMonth(),theDate.getNbrDayOfMonth());
		assertEquals(D.getYear(),         theDate.getYear());
		
		assertEquals(theDate.getNbrMonth(),     "11");	
		assertEquals(theDate.getNbrDayOfMonth(),"19");
		assertEquals(theDate.getYear(),         2016); 
		
		assertTrue(theDate.getIsoDateValid());
	}
	
	// // // //
	
	@Test
	/*
	 * Create a LiturgicalDayProperties object with a valid month and day
	 */
	public void testLDPGregorianValidMonthDayConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		String month = today.getNbrMonth();
		String day   = today.getNbrDayOfMonth();
		int    year  = today.getYear();
		
		LiturgicalDayProperties D     = new LiturgicalDayProperties(month,day);
    
		assertEquals(D.getNbrMonth(),     month);	
		assertEquals(D.getNbrDayOfMonth(),day);
		assertEquals(D.getYear(),         year + 1); // for current month & day, date is next year
	}
		
	@Test
	/*
	 * Create a LiturgicalDayProperties object with an invalid month
	 */
	public void testLDPGregorianINVALIDMonthInMonthDayConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		String month = "13";
		String modMonth = Integer.toString(Integer.parseInt(month) % 12);
		if (modMonth.length() == 1)
			modMonth = "0" + modMonth;
		
		String day   = today.getNbrDayOfMonth();
		
		int    year  = today.getYear() + 1; // The invalid month, 13, wraps to 1
        // and increments the year.
		
		LiturgicalDayProperties D = new LiturgicalDayProperties(month,day);

		assertEquals(D.getNbrMonth(),     modMonth);
		assertEquals(D.getNbrDayOfMonth(),day);
		assertEquals(D.getYear(),         year); 
	}
	
	@Test
	/*
	 * Create a LiturgicalDayProperties object with an invalid day of the month
	 * 
	 * NOTE: There appears to be no way to detect that the date is invalid
	 */
	public void testLDPGregorianINVALIDDayInMonthDayConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		String month = today.getNbrMonth();
		String nextMonth = Integer.toString(Integer.parseInt(month)+1);
		if (nextMonth.length() == 1)
			nextMonth = "0" + nextMonth;
		
		String day   = "32";
		String wrapDay = Integer.toString(Integer.parseInt(day) % daysInMonth(month));
		if (wrapDay.length() == 1)
			wrapDay = "0" + wrapDay;
		
		int    year  = today.getYear();
		
		LiturgicalDayProperties D = new LiturgicalDayProperties(month,day);
	    
		assertEquals(D.getNbrMonth(),     nextMonth);	
		assertEquals(D.getNbrDayOfMonth(),wrapDay);	 	
		assertEquals(D.getYear(),         year); 
	}
	
	// // // //
	
	@Test
	/*
	 * Create a LiturgicalDayProperties object with a valid year, month and day
	 */
	public void testLDPGregorianValidYearMonthDayConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		String month = today.getNbrMonth();
		String day   = today.getNbrDayOfMonth();
		int    year  = today.getYear();
		
		LiturgicalDayProperties D     = new LiturgicalDayProperties(Integer.toString(year),
				                                                    month,
                                                                    day);
		
		assertEquals(D.getNbrMonth(),     month);	
		assertEquals(D.getNbrDayOfMonth(),day);
		assertEquals(D.getYear(),         year);		
	}
	
	@Test
	/*
	 * Create a LiturgicalDayProperties object with an invalid year (Valid month and day)
	 * 
	 * NOTE: There appears to be no way to detect that the date is invalid
	 */
	public void testLDPGregorianINVALIDYearInYearMonthDayConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		String month = today.getNbrMonth();
		String day   = today.getNbrDayOfMonth();
		
		LiturgicalDayProperties D     = new LiturgicalDayProperties("0000",
				                                                    today.getNbrMonth(),
                                                                    today.getNbrDayOfMonth());
		
		assertEquals(D.getNbrMonth(),     month);	
		assertEquals(D.getNbrDayOfMonth(),day);
		assertEquals(D.getYear(),         1);	// The invalid year, 0, is set to 1	
	}
	
	@Test
	/*
	 * Create a LiturgicalDayProperties object with an invalid month (Valid year and day)
	 * 
	 * NOTE: There appears to be no way to detect that the date is invalid
	 */
	public void testLDPGregorianINVALIDMonthInYearMonthDayConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		String month = "13";
		String modMonth = Integer.toString(Integer.parseInt(month) % 12);
		if (modMonth.length() == 1)
			modMonth = "0" + modMonth;
		
		String day   = today.getNbrDayOfMonth();
		
		int    year  = today.getYear() + 1; // The invalid month, 13, wraps to 1
        // and increments the year.
		
		LiturgicalDayProperties D = new LiturgicalDayProperties(month,day);

		assertEquals(D.getNbrMonth(),     modMonth);
		assertEquals(D.getNbrDayOfMonth(),day);
		assertEquals(D.getYear(),         year); 
	}
	
	@Test
	/*
	 * Create a LiturgicalDayProperties object with an invalid day of the month
	 * 
	 * NOTE: There appears to be no way to detect that the date is invalid
	 */
	public void testLDPGregorianINVALIDDayInYearMonthDayConstructor() {
		LiturgicalDayProperties today = new LiturgicalDayProperties(); // today
		
		String month = today.getNbrMonth();
		String nextMonth = Integer.toString(Integer.parseInt(month)+1);
		if (nextMonth.length() == 1)
			nextMonth = "0" + nextMonth;
		
		String day   = "32";
		String wrapDay = Integer.toString(Integer.parseInt(day) % daysInMonth(month));
		if (wrapDay.length() == 1)
			wrapDay = "0" + wrapDay;
		
		int    year  = today.getYear() + 1; // This is *after* the current date	
		
		LiturgicalDayProperties D     = new LiturgicalDayProperties(Integer.toString(year),
				                                                    month,
                                                                    day);

		assertEquals(D.getNbrMonth(),     nextMonth); // Apparently, the invalid month is set to 1
		assertEquals(D.getNbrDayOfMonth(),wrapDay);
		assertEquals(D.getYear(),         year); 
		}
	
	// // // //
	
	@Test
	public void testLDPGregorianValidLongValueConstructor() {
		GregorianCalendar today = new GregorianCalendar();		
		long longMillis    = today.getTimeInMillis();
		
		LiturgicalDayProperties D1 = new LiturgicalDayProperties(longMillis);
		LiturgicalDayProperties D2 = new LiturgicalDayProperties();
		
		assertEquals(D1.getNbrMonth(),     D2.getNbrMonth());	
		assertEquals(D1.getNbrDayOfMonth(),D2.getNbrDayOfMonth());	 	
		assertEquals(D1.getYear(),         D2.getYear()); 
	}
	
	@Test
	public void testLDPSetDateTo() {

		LiturgicalDayProperties D1 = new LiturgicalDayProperties("2016","4","30");
		LiturgicalDayProperties D2 = new LiturgicalDayProperties();
		D2.setDateTo("2016", "4", "30");
		
		assertEquals(D1.getNbrMonth(),     D2.getNbrMonth());	
		assertEquals(D1.getNbrDayOfMonth(),D2.getNbrDayOfMonth());	 	
		assertEquals(D1.getYear(),         D2.getYear()); 
	}
	
	@Test
	public void testLDPresetDate() {

		LiturgicalDayProperties today = new LiturgicalDayProperties();
		
		LiturgicalDayProperties D1 = new LiturgicalDayProperties("2016","4","30");
		
		LiturgicalDayProperties D2 = new LiturgicalDayProperties(Integer.toString(today.getYear()),
				                                                 today.getNbrMonth(),
				                                                 today.getNbrDayOfMonth()); // originally today
		
		assertEquals(today.getNbrMonth(),     D2.getNbrMonth());	
		assertEquals(today.getNbrDayOfMonth(),D2.getNbrDayOfMonth());	 	
		assertEquals(today.getYear(),         D2.getYear()); 
		
		D2.setDateTo("2016", "4", "30");
		
		assertEquals(D1.getNbrMonth(),     D2.getNbrMonth());	
		assertEquals(D1.getNbrDayOfMonth(),D2.getNbrDayOfMonth());	 	
		assertEquals(D1.getYear(),         D2.getYear()); 
		
		D2.resetDate(); // back to today
		
		assertEquals(today.getNbrMonth(),     D2.getNbrMonth());	
		assertEquals(today.getNbrDayOfMonth(),D2.getNbrDayOfMonth());	 	
		assertEquals(today.getYear(),         D2.getYear()); 
	}
	
	//@Test
	public void testLDPtimeDelta() {
	}
	
	//@Test
	public void testLDPsetLiturgicalPropertiesByDate() {
	}
	
	//@Test
	public void testLDPgetCalendarForElevationOfCross() {
	}
	
	//@Test
	public void testLDPgetCalendarForSundayAfterElevationOfCross() {
	}
	
	//@Test
	public void testLDPgetFormattedSundayAfterElevationOfCross() {
	}
	
	//@Test
	public void testLDPgetMonthOfSundayAfterElevationOfCross() {
	}
	
	//@Test
	public void testLDPgetDayOfSundayAfterElevationOfCross() {
	}
	
	//@Test
	public void testLDPgetStartOfLukanCycle() {
	}
	
	//@Test
	public void testLDPsetDateLastTriodionStart() { // ???
	}
	
	//@Test
	public void testLDPsetElevationOfCross() { // ???
	}
	
	//@Test
	public void testLDPsetDateStartLukanCycle() { // ???
	}
	
	//@Test
	public void testLDPcomputeDayOfPascha() {
	}
	
	//@Test
	public void testLDPgetModeOfWeek() {
	}
	
	//@Test
	public void testLDPsetModeOfTheWeek() {
	}
	
	//@Test
	public void testLDPgetModeOfWeekOverride() {
	}
	
	//@Test
	public void testLDPsetModeOfTheWeekOVERRIDE() {
	}
	
	//@Test
	public void testLDPdiffMills() {
	}
	
	//@Test
	public void testLDPisPascha() {
	}
	
	//@Test
	public void testLDPgetEothinNumber() {
	}
	
	//@Test
	public void testLDPsetEothinNumber() {
	}
	
	//@Test
	public void testLDPsetEothinNumberDEFAULT() {
	}
	
	//@Test
	public void testLDPgetDayOfSeason() {
	}
	
	//@Test
	public void testLDPgetDaysSinceSundaysAfterLastElevationOfCross() {
	}
	
	//@Test
	public void testLDPgetDaysSinceStartLukanCycle() {
	}
	
	//@Test
	public void testLDPgetWeekAndDayOfLukanCycle() {
	}
	
	//@Test
	public void testLDPgetNumberDegree() {
	}
	
	//@Test
	public void testLDPgetWeekOfLukanCycle() {
	}
	
	//@Test
	public void testLDPgetDaysSinceStartOfTriodion() {
	}
	
	//@Test
	public void testLDPtriodionDayToMovableDay() {
	}
	
	//@Test
	public void testLDPgetNameOfPeriod() {
	}
	
	//@Test
	public void testLDPgetGregorianCalendar() {
	}
	
	//@Test
	public void testLDPgetNameOfDayAbbreviation() {
	}
	
	//@Test
	public void testLDPgetNameOfMonth() {
	}
	
	//@Test
	public void testLDPgetNbrMonth() {
	}
	
	//@Test
	public void testLDPgetIntMonth() {
	}
	
	//@Test
	public void testLDPsetNbrMonthINT() {
	}
	
	//@Test
	public void testLDPgetNbrDayOfMonth() {
	}
	
	//@Test
	public void testLDPgetIntDayOfMonth() {
	}
	
	//@Test
	public void testLDPsetNbrDayOfMonthINT() {
	}
	
	//@Test
	public void testLDPgetNbrDayOfWeek() {
	}
	
	//@Test
	public void testLDPgetIntWeekOfLent() {
	}
	
	//@Test
	public void testLDPgetIntDayOfWeek() {
	}
	
	//@Test
	public void testLDPsetNbrDayOfWeekINT() {
	}
	
	//@Test
	public void testLDPgetNbrDayOfWeekOVERRIDE() {
	}
	
	//@Test
	public void testLDPsetNbrDayOfWeekOVERRIDE() {
	}
	
	//@Test
	public void testLDPgetYear() {
	}
	
	//@Test
	public void testLDPgetDisplayDate() {
	}
	
	//@Test
	public void testLDPgetDisplayDateINVALIDFORMAT() {
	}
	
	//@Test
	public void testLDPgetTheDayBeforeAsPath() {
	}
	
	//@Test
	public void testLDPgetTheDayAsPath() {
	}
	
	//@Test
	public void testLDPgetDisplayDateForLocale() {
	}
	
	//@Test
	public void testLDPgetTheDay() {
	}
	
	//@Test
	public void testLDPsetTheDay() {
	}
	
	//@Test
	public void testLDPgetTheDayBefore() {
	}
	
	//@Test
	public void testLDPsetTheDayBefore() {
	}
	
	//@Test
	public void testLDPgetPaschaDate() {
	}
	
	//@Test
	public void testLDPgetNextPaschaDate() {
	}
	
	//@Test
	public void testLDPsetPaschaDate() {
	}
	
	//@Test
	public void testLDPgetPentecostDate() {
	}
	
	//@Test
	public void testLDPsetPentecostDate() {
	}
	
	//@Test
	public void testLDPgetPalmSundayDate() {
	}
	
	//@Test
	public void testLDPsetPalmSundayDate() {
	}
	
	//@Test
	public void testLDPgetAllSaintsDate() {
	}
	
	//@Test
	public void testLDPsetAllSaintsDate() {
	}
	
	//@Test
	public void testLDPsetAllSaintsDateLastYear() {
	}
	
	//@Test
	public void testLDPgetGreatLentStartDate() {
	}
	
	//@Test
	public void testLDPsetGreatLentStartDate() {
	}
	
	//@Test
	public void testLDPgetTriodionStartDateThisYear() {
	}
	
	//@Test
	public void testLDPsetTriodionStartDateThisYear() {
	}
	
	//@Test
	public void testLDPgetTriodionStartDateNextYear() {
	}
	
	//@Test
	public void testLDPsetTriodionStartDateNextYear() {
	}
	
	//@Test
	public void testLDPgetTriodionStartDateLastYear() {
	}
	
	//@Test
	public void testLDPsetTriodionStartDateLastYear() {
	}
	
	//@Test
	public void testLDPisPentecostarion() {
	}
	
	//@Test
	public void testLDPsetIsPentecostarion() {
	}
	
	//@Test
	public void testLDPisTriodion() {
	}
	
	//@Test
	public void testLDPsetIsTriodion() {
	}
	
	//@Test
	public void testLDPisSunday() {
	}
	
	//@Test
	public void testLDPsetIsSunday() {
	}
	
	//@Test
	public void testLDPisMonday() {
	}
	
	//@Test
	public void testLDPsetIsMonday() {
	}
	
	//@Test
	public void testLDPisTuesday() {
	}
	
	//@Test
	public void testLDPsetIsTuesday() {
	}
	
	//@Test
	public void testLDPisWednesday() {
	}
	
	//@Test
	public void testLDPsetIsWednesday() {
	}
	
	//@Test
	public void testLDPisThursday() {
	}
	
	//@Test
	public void testLDPsetIsThursday() {
	}
	
	//@Test
	public void testLDPisFriday() {
	}
	
	//@Test
	public void testLDPsetIsFriday() {
	}
	
	//@Test
	public void testLDPisSaturday() {
	}
	
	//@Test
	public void testLDPsetIsSaturday() {
	}
	
	//@Test
	public void testLDPtoHtml() {
	}
	
	//@Test
	public void testLDPallDatesToString() {
	}
	
	//@Test
	public void testLDPtheDayAndSeasonToString() {
	}
	
	//@Test
	public void testLDPelevationToString() {
	}
	
	//@Test
	public void testLDPtoString() {
	}
	
	//@Test
	public void testLDPgetFormattedYearMonthDay() {
	}
	
	//@Test
	public void testLDPformattedDate() {
	}
	
	//@Test
	public void testLDPpathDate() {
	}
	
	//@Test
	public void testLDPgetDateAsPath() {
	}
	
	//@Test
	public void testLDPgetDayAsFileName() {
	}
	
	//@Test
	public void testLDPgetFormattedDatePath() {
	}
	
	//@Test
	public void testLDPgetFormattedDate() {
	}
	
	//@Test
	public void testLDPdivWrapStringString() {
	}
	
	//@Test
	public void testLDPdivWrapStringBoolean() {
	}
	
	//@Test
	public void testLDPstrWrapStringBoolean() {
	}
	
	//@Test
	public void testLDPstrWrapStringString() {
	}
	
	//@Test
	public void testLDPformattedNumber() {
	}
	
	//@Test
	public void testLDPgetDiffSeason() {
	}
	
	//@Test
	public void testLDPcheckDates() {
	}
	
	//@Test
	public void testLDPnextPaschaDate() {
	}
	
	//@Test
	public void testLDPlastPaschaDate() {
	}
	
	//@Test
	public void testLDPcheckPaschas() {
	}
	
	//@Test
	public void testLDPgetTriodopnStartDateLast() {
	}
	
	//@Test
	public void testLDPsetTriodopnStartDateLast() {
	}
	
	//@Test
	public void testLDPgetPaschaDateLastYear() {
	}
	
	//@Test
	public void testLDPsetPaschaDateLastYear() {
	}
	
	//@Test
	public void testLDPgetPaschaDateLast() {
	}
	
	//@Test
	public void testLDPsetPaschaDateLast() {
	}
	
	//@Test
	public void testLDPgetPaschaDateThisYear() {
	}
	
	//@Test
	public void testLDPsetPaschaDateThisYear() {
	}
	
	//@Test
	public void testLDPgetPaschaDateNext() {
	}
	
	//@Test
	public void testLDPsetPaschaDateNext() {
	}
	
	//@Test
	public void testLDPgetElevationOfCrossLast() {
	}
	
	//@Test
	public void testLDPsetElevationOfCrossLast() {
	}
	
	//@Test
	public void testLDPgetSundayAfterElevationOfCrossLast() {
	}
	
	//@Test
	public void testLDPsetSundayAfterElevationOfCrossLast() {
	}
	
	//@Test
	public void testLDPgetStartDateOfLukanCycle() {
	}
	
	//@Test
	public void testLDPsetStartDateOfLukanCycle() {
	}
	
	//@Test
	public void testLDPgetAllSaintsDayThisYear() {
	}
	
	//@Test
	public void testLDPsetAllSaintsDayThisYear() {
	}
	
	//@Test
	public void testLDPgetPalmSundayNextDate() {
	}
	
	//@Test
	public void testLDPsetPalmSundayNextDate() {
	}
	
	//@Test
	public void testLDPgetThomasSundayDate() {
	}
	
	//@Test
	public void testLDPsetThomasSundayDate() {
	}
	
	//@Test
	public void testLDPgetLazarusSaturdayNextDate() {
	}
	
	//@Test
	public void testLDPsetLazarusSaturdayNextDate() {
	}
	
	//@Test
	public void testLDPgetDaysUntilStartOfTriodion() {
	}
	
	//@Test
	public void testLDPsetDaysUntilStartOfTriodion() {
	}
	
	//@Test
	public void testLDPgetDaysSinceStartLastLukanCycle() {
	}
	
	//@Test
	public void testLDPsetDaysSinceStartLastLukanCycle() {
	}
	
	//@Test
	public void testLDPgetIsPascha() {
	}
	
	//@Test
	public void testLDPsetIsPascha() {
	}
	
	//@Test
	public void testLDPgetIsDayOfLuke() {
	}
	
	//@Test
	public void testLDPsetIsDayOfLuke() {
	}
	
	//@Test
	public void testLDPgetIsoDateValid() {
	}
	
	//@Test
	public void testLDPsetIsoDateValid() {
	}
	
	//@Test
	public void testLDPgetDayOfWeek() {
	}
	
	//@Test
	public void testLDPsetDayOfWeek() {
	}
	
	//@Test
	public void testLDPgetDayOfWeekOVERRIDE() {
	}
	
	//@Test
	public void testLDPsetDayOfWeekOVERRIDE() {
	}
	
	//@Test
	public void testLDPgetGreekMonths() {
	}
	
	//@Test
	public void testLDPsetGreekMonths() {
	}
	
	//@Test
	public void testLDPgetGreekWeekDays() {
	}
	
	//@Test
	public void testLDPsetGreekWeekDays() {
	}
	
	//@Test
	public void testLDPsetGreekMonthDays() {
	}
	
	//@Test
	public void testLDPgetWordBoundary() {
	}
	
	//@Test
	public void testLDPsetWordBoundary() {
	}
	
	//@Test
	public void testLDPgetGreekMap() {
	}
	
	//@Test
	public void testLDPsetGreekMap() {
	}
	
	//@Test
	public void testLDPgetOriginalYear() {
	}
	
	//@Test
	public void testLDPsetOriginalYear() {
	}
	
	//@Test
	public void testLDPgetOriginalMonth() {
	}
	
	//@Test
	public void testLDPsetOriginalMonth() {
	}
	
	//@Test
	public void testLDPgetOriginalDay() {
	}
	
	//@Test
	public void testLDPsetOriginalDay() {
	}
	
	//@Test
	public void testLDPgetOriginalDayOfSeason() {
	}
	
	//@Test
	public void testLDPsetOriginalDayOfSeason() {
	}
	
	//@Test
	public void testLDPgetAllSaintsDayLastYear() {
	}
	
	//@Test
	public void testLDPgetIsPentecostarion() {
	}
	
	//@Test
	public void testLDPgetIsTriodion() {
	}
	
	//@Test
	public void testLDPgetIsSunday() {
	}
	
	//@Test
	public void testLDPgetIsMonday() {
	}
	
	//@Test
	public void testLDPgetIsTuesday() {
	}
	
	//@Test
	public void testLDPgetIsWednesday() {
	}
	
	//@Test
	public void testLDPgetIsThursday() {
	}
	
	//@Test
	public void testLDPgetIsFriday() {
	}
	
	//@Test
	public void testLDPgetIsSaturday() {
	}
	
	//@Test
	public void testLDPsetModeOfWeek() {
	}
	
	//@Test
	public void testLDPsetModeOfWeekOVERRIDE() {
	}
	
	//@Test
	public void testLDPsetDaysSinceStartOfTriodion() {
	}
	
	//@Test
	public void testLDPsetDaysSinceSundayAfterLastElevationOfCross() {
	}
	
	//@Test
	public void testLDPsetNumberOfSundaysBeforeStartOfTriodion() {
	}
	
	//@Test
	public void testLDPsetNbrDayOfWeekSTRING() {
	}
	
	//@Test
	public void testLDPsetNbrDayOfMonthSTRING() {
	}
	
	//@Test
	public void testLDPsetNbrMonthSTRING() {
	}
	
	//@Test
	public void testLDPisUseGregorianCalendar() {
	}
	
	//@Test
	public void testLDPsetUseGregorianCalendar() {
	}
	
	//@Test
	public void testLDPgetFormattedJulianDate() {
	}
	
	//@Test
	public void testLDPgetTheDayAsJulian() {
	}
	
	//@Test
	public void testLDPadjustToJulianDate() {
	}
	
	//@Test
	public void testLDPadjustToGregorianDate() {
	}
	
	//@Test
	public void testLDPgetJulianOffset() {
	}
	
	//@Test
	public void testLDPsetJulianOffset() {
	}
	
	//@Test
	public void testLDPgetCaledarType() {
	}
	
	//@Test
	public void testLDPsetCaledarType() {
	}
	
	//@Test
	public void testLDPgetTheDayGregorian() {
	}
	
	//@Test
	public void testLDPsetTheDayGregorian() {
	}
	
	//@Test
	public void testLDPgetTheDayJulian() {
	}
	
	//@Test
	public void testLDPsetTheDayJulian() {
	}
	
}
