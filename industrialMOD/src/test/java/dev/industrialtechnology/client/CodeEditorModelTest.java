package dev.industrialtechnology.client;
import org.junit.Test;
import static org.junit.Assert.*;
public class CodeEditorModelTest {
    @Test public void multilineInsertionAndSelectionReplacement(){CodeEditorModel m=new CodeEditorModel();m.insert("WHEN\nTHEN\nEND");assertEquals(3,m.lines().length);m.move(0,false);m.move(4,true);assertEquals("WHEN",m.selected());m.insert("when");assertEquals("when\nTHEN\nEND",m.text());}
    @Test public void backspaceAndDeleteCrossLineBoundaries(){CodeEditorModel m=new CodeEditorModel();m.set("a\nb");m.move(2,false);m.back();assertEquals("ab",m.text());m.undo();m.move(1,false);m.delete();assertEquals("ab",m.text());}
    @Test public void verticalMovementRetainsDesiredColumn(){CodeEditorModel m=new CodeEditorModel();m.set("12345\nx\n12345");m.move(4,false);m.vertical(1,false);assertEquals(7,m.cursor);m.vertical(1,false);assertEquals(12,m.cursor);}
    @Test public void clipboardTextNormalizesNewlinesAndHonorsLimit(){CodeEditorModel m=new CodeEditorModel();m.limit=5;assertTrue(m.insert("a\r\nb"));assertEquals("a\nb",m.text());assertFalse(m.insert("123"));assertEquals("a\nb",m.text());m.all();assertTrue(m.insert("12345"));assertEquals("12345",m.text());}
    @Test public void undoRedoRestoresTextAndSelection(){CodeEditorModel m=new CodeEditorModel();m.insert("abc");m.all();m.insert("x");m.undo();assertEquals("abc",m.selected());m.redo();assertEquals("x",m.text());}
    @Test public void cursorAndSelectionStayInRange(){CodeEditorModel m=new CodeEditorModel();m.set("x");m.move(-8,false);assertEquals(0,m.cursor);m.move(999,true);assertEquals("x",m.selected());m.vertical(99,false);assertEquals(1,m.cursor);}
}
