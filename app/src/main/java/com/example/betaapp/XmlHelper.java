package com.example.betaapp;

import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlHelper {
    private static Document doc;
    private static Element root;

    private static boolean isFirstActivity; // which activity start the init (in first activity we need to save the xml name is database)

    public static boolean init(String xmlPath, boolean firstActivity)
    {
        boolean goodInit = false;

        isFirstActivity = firstActivity;

        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            //dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            doc = db.parse(new File(xmlPath));

            // normalize the data - (hello\n world would be hello world)
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            root = doc.getDocumentElement();

            goodInit = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return goodInit;
    }

    // finish yer - the year that student will finish school

    public static void pushData(HashMap<String, String> data, String destPath, String finishYear)
    {
        // replace the elements in the xml file
        for (Map.Entry<String, String> mapElement : data.entrySet()) {
            Node element = root.getElementsByTagName(mapElement.getKey()).item(0);
            element.setTextContent(mapElement.getValue());
        }

        // write the xml file
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(doc);

            File outputFile = new File(destPath);
            transformer.transform(source, new StreamResult(outputFile));

            uploadFileToFirebase(destPath, finishYear);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> getData()
    {
        HashMap<String, String> data = new HashMap<>();
        String value = "";
        NodeList questions = root.getChildNodes();

        for (int temp = 0; temp < questions.getLength(); temp++)
        {
            Node node = questions.item(temp);

            // don't get the '\n' or '\t' nodes
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                // each question's detail
                Element element = (Element) node;

                value = "";
                if (element.hasChildNodes())
                {
                    value = element.getChildNodes().item(0).getTextContent();
                }

                data.put(node.getNodeName(), value);
            }
        }

        return data;
    }

    private static void uploadFileToFirebase(String filePath, String finishYear)
    {
        Uri file = Uri.fromFile(new File(filePath));
        UploadTask uploadTask = FBref.storageRef.child("/forms").child(finishYear).child(file.getLastPathSegment()).putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...

                if (isFirstActivity)
                {
                    // update the registrationFormID path link (in the student in firebase)
                    FBref.refStudents.child(file.getLastPathSegment().split("\\.")[0]).child("registrationFormID").setValue(finishYear + "/" + file.getLastPathSegment());

                    isFirstActivity = false;
                }
            }
        });
    }
}
