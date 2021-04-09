package com.choicely.preassignmentproject.data;

import android.util.Xml;

import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class DataPayloadXMLParser {

    public static String parseDataPayloadXML(String availabilityXML) {
        XmlPullParser parser = Xml.newPullParser();
        InputStream stream = IOUtils.toInputStream(availabilityXML);
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            parser.nextTag();
            String availabilityData = readDataPayload(parser);
            stream.close();
            return availabilityData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String readDataPayload(XmlPullParser parser) {
        try {
            parser.require(XmlPullParser.START_TAG, null, "AVAILABILITY");
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                if (parser.getName().equals("INSTOCKVALUE")) {
                    parser.require(XmlPullParser.START_TAG, null, "INSTOCKVALUE");
                    if (parser.next() == XmlPullParser.TEXT) {
                        return parser.getText();
                    } else {
                        System.out.println("it wasn't text");
                    }
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return "no instockvalue found";
    }
}
