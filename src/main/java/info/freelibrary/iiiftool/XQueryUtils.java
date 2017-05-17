
package info.freelibrary.iiiftool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.basex.core.Context;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.query.value.item.Item;

/**
 * A little convenience class because I find XQuery so much nicer than JsonPath. The methods expect one liner XQueries.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class XQueryUtils {

    private static final String QUERY = "declare variable $json as xs:string external;";

    private static final Context CONTEXT = new Context();

    private XQueryUtils() {
    }

    public static final List<String> getList(final String aJson, final String aXQuery) {
        final QueryProcessor qp = new QueryProcessor(QUERY + " parse-json($json)" + aXQuery, CONTEXT);
        final List<String> list = new ArrayList<>();
        final Iter iter;

        try {
            Item item;

            qp.bind("json", aJson);
            iter = qp.iter();

            while ((item = iter.next()) != null) {
                list.add((String) item.toJava());
            }
        } catch (final QueryException details) {
            throw new RuntimeException(details);
        }

        return list;
    }

    public static final String getValue(final String aJson, final String aXQuery) {
        final QueryProcessor qp = new QueryProcessor(QUERY + " parse-json($json)" + aXQuery, CONTEXT);
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final String value;
        final Iter iter;

        try {
            qp.bind("json", aJson);
            iter = qp.iter();

            try (Serializer ser = qp.getSerializer(bytes)) {
                for (Item item; (item = iter.next()) != null;) {
                    ser.serialize(item);
                }
            } catch (final IOException details) {
                throw new RuntimeException(details);
            }

            value = bytes.toString(StandardCharsets.UTF_8.name());
        } catch (final QueryException | UnsupportedEncodingException details) {
            throw new RuntimeException(details);
        }

        return value;
    }
}
