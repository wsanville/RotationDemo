package co.touchlab.rotationdemo.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * User: William Sanville
 * Date: 10/8/12
 * Time: 12:26 PM
 * Static helper methods for loading weather info.
 */
public class Weather
{
    private final static String ZIP_LOOKUP_URL = "http://graphical.weather.gov/xml/sample_products/browser_interface/ndfdXMLclient.php?listZipCodeList=%s";
    private final static String WEATHER_LOOKUP_URL = "http://forecast.weather.gov/MapClick.php?lat=%.4f&lon=%.4f&unit=0&lg=english&FcstType=dwml";

    private Weather() { }

    public static WeatherInfo weatherByZipCode(String zip) throws IOException, SAXException, ParserConfigurationException
    {
        String zipLookupUrl = String.format(ZIP_LOOKUP_URL, zip);
        String latLonXml = WebHelper.performGet(zipLookupUrl);
        LatLonPair pair = LatLonPair.fromXml(latLonXml);

        String weatherUrl = String.format(WEATHER_LOOKUP_URL, pair.getLatitude(), pair.getLongitude());
        String weatherXml = WebHelper.performGet(weatherUrl);
        return WeatherInfo.fromXml(weatherXml, zip);
    }

    private static Document getDocument(String xml) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));

        return db.parse(is);
    }

    public static class LatLonPair
    {
        private float latitude, longitude;

        public LatLonPair(float latitude, float longitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public float getLatitude()
        {
            return latitude;
        }

        public float getLongitude()
        {
            return longitude;
        }

        public static LatLonPair fromXml(String xml) throws ParserConfigurationException, IOException, SAXException
        {
            Document doc = getDocument(xml);
            Node node = doc.getDocumentElement().getChildNodes().item(0);
            String textContent = node.getTextContent();

            String[] parts = textContent.split(",");
            return new LatLonPair(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
        }
    }

    public static class WeatherInfo
    {
        private String zip, locationName;
        private float temperature;

        public WeatherInfo(String locationName, float temperature, String zip)
        {
            this.locationName = locationName;
            this.temperature = temperature;
            this.zip = zip;
        }

        public String getLocationName()
        {
            return locationName;
        }

        public float getTemperature()
        {
            return temperature;
        }

        public String getZip()
        {
            return zip;
        }

        /**
         * Parses a really simply subset of all the info returned.
         */
        public static WeatherInfo fromXml(String xml, String zip) throws IOException, SAXException, ParserConfigurationException
        {
            String locationName = null;
            float temp = Float.MIN_VALUE;

            Document doc = getDocument(xml);
            NodeList nodes = doc.getElementsByTagName("data");
            for (int i = 0, l = nodes.getLength(); i < l; i++)
            {
                Element current = (Element)nodes.item(i);
                Node type = current.getAttributes().getNamedItem("type");
                String typeValue = type.getTextContent();
                if (typeValue.equals("forecast"))
                {
                    //Read the display location
                    Element location = (Element)current.getElementsByTagName("location").item(0);
                    Node description = location.getElementsByTagName("description").item(0);
                    locationName = description.getTextContent();
                }
                else if (typeValue.equals("current observations"))
                {
                    //Read the current temperature
                    temp = readTemp(current);
                }
            }

            if (temp == Float.MIN_VALUE)
                return null;
            else
                return new WeatherInfo(locationName, temp, zip);
        }

        private static float readTemp(Element current)
        {
            Element parameters = (Element)current.getElementsByTagName("parameters").item(0);
            NodeList temperatureNodes = parameters.getElementsByTagName("temperature");
            for (int t = 0, len = temperatureNodes.getLength(); t < len; t++)
            {
                Node temperatureNode = temperatureNodes.item(t);
                Node tempType = temperatureNode.getAttributes().getNamedItem("type");
                if (tempType.getTextContent().equals("apparent"))
                {
                    String value = ((Element)temperatureNode).getElementsByTagName("value").item(0).getTextContent();
                    return Float.parseFloat(value);
                }
            }

            return Float.MIN_VALUE;
        }
    }
}
