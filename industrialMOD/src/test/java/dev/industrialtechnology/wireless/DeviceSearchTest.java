package dev.industrialtechnology.wireless;
import org.junit.Test;import static org.junit.Assert.*;
public class DeviceSearchTest {
    @Test public void partialCaseInsensitiveNames(){assertTrue(DeviceSearch.matches("rBmK","Main RBMK Fuel 01","fuel",0,1,2,3));assertFalse(DeviceSearch.matches("tank","RBMK Fuel 01","fuel",0,1,2,3));}
    @Test public void emptyAndWhitespaceSearchShowsAll(){assertTrue(DeviceSearch.matches("  ","Pump","machine",0,0,0,0));assertTrue(DeviceSearch.matches(null,"","",0,0,0,0));}
    @Test public void metadataAndCoordinatesCanBeSearched(){assertTrue(DeviceSearch.matches("zirnox","Primary","ZIRNOX Reactor",-1,20,70,30));assertTrue(DeviceSearch.matches("20, 70","Primary","machine",0,20,70,30));}
    @Test public void pagesClampAfterFilterShrinks(){assertEquals(1,DeviceSearch.pages(0));assertEquals(17,DeviceSearch.pages(100));assertEquals(0,DeviceSearch.page(16,3));assertEquals(16,DeviceSearch.page(99,100));assertEquals(0,DeviceSearch.page(-1,100));}
}
