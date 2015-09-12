import com.ua.httpnettyserver.HtmlResponseCreator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ARTUR on 05.09.2015.
 */
public class HtmlCreatorTest {
    @Test
    public void testEmptyHtmlCreating() {
        HtmlResponseCreator response = HtmlResponseCreator.create();
        // if resource is read successfully, then it'll have <title> tag
        Assert.assertTrue(response.toString().contains("<title>"));
    }
}
