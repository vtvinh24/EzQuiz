package dev.vtvinh24.ezquiz.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.util.List;

import dev.vtvinh24.ezquiz.data.model.Quiz;

public class QuizImporterTest {
  @Test
  public void testImportFlashcardsFromPasteRs() {
    String url = "JpOIB";
    QuizImporter importer = new QuizImporter();
    List<Quiz> quizzes = importer.importFlashcards(url);
    assertNotNull(quizzes);
    assertFalse(quizzes.isEmpty());
    Quiz first = quizzes.get(0);
    assertEquals("completing the square when a=0", first.getQuestion());
    assertEquals(1, first.getAnswers().size());
    assertEquals("( x - b/2 )^2 - ( b/2 )^2 - c", first.getAnswers().get(0));
  }

  @Test
  public void testImportFlashcardsFromRawJson() {
    String json = "[{'front':'completing the square when a=0','back':'( x - b/2 )^2 - ( b/2 )^2 - c'},{'front':'completing the square when a/=/0','back':'n ( x - b/2a )^2 - c - b^2/2(2a)'},{'front':'even number','back':'2n'},{'front':'odd number','back':'2n + 1'},{'front':'consecutive numbers','back':'n, n + 1, n + 2'},{'front':'graph translation on the y-axis','back':'(upwards) y = f(x) + a \n(downwards) y = f(x) - a'},{'front':'graph translation on the x-axis','back':'(left) y = f(x + a)\n(right) y = f(x - a)'},{'front':'graph reflections','back':'(x-axis) y = -f(x)\n(y-axis) y = f(-x)'},{'front':'direct proportion','back':'DIVIDE for ONE, then TIMES for ALL'},{'front':'inverse proportion','back':'TIMES for ONE, then DIVIDE for all'}]".replace("'", "\"");
    QuizImporter importer = new QuizImporter();
    // We'll use the same logic as importFlashcards, but directly on the JSON string
    List<Quiz> quizzes = importer.importFlashcardsFromJson(json);
    assertNotNull(quizzes);
    assertFalse(quizzes.isEmpty());
    Quiz first = quizzes.get(0);
    assertEquals("completing the square when a=0", first.getQuestion());
    assertEquals(1, first.getAnswers().size());
    assertEquals("( x - b/2 )^2 - ( b/2 )^2 - c", first.getAnswers().get(0));
  }
}
