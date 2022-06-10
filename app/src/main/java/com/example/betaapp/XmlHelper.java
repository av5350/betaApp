package com.example.betaapp;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlHelper {
    private static Document doc;
    private static Element root;

    private static boolean isFirstActivity; // which activity start the init (in first activity we need to save the xml path and finish year in database)

    public static void init(String xmlPath, boolean firstActivity)
    {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // finish yer - the year that student will finish school

    public static void pushData(HashMap<String, String> data)
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

            File outputFile = new File(Helper.studentFormDestPath);
            transformer.transform(source, new StreamResult(outputFile));

            uploadFileToFirebase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> getData(ArrayList<String> wantedTags)
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

                if (wantedTags.contains(element.getTagName()))
                {
                    value = "";
                    if (element.hasChildNodes())
                    {
                        value = element.getChildNodes().item(0).getTextContent();
                    }

                    data.put(node.getNodeName(), value);
                }
            }
        }

        return data;
    }

    private static void uploadFileToFirebase()
    {
        Uri file = Uri.fromFile(new File(Helper.studentFormDestPath));
        UploadTask uploadTask = FBref.storageRef.child("/forms").child("" + Helper.studentFinishYear).child(file.getLastPathSegment()).putFile(file);

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
                    FBref.refStudents.child(file.getLastPathSegment().split("\\.")[0]).child("registrationFormID").setValue(Helper.studentFinishYear + "/" + file.getLastPathSegment());
                    FBref.refStudents.child(file.getLastPathSegment().split("\\.")[0]).child("finishYear").setValue(Helper.studentFinishYear);

                    isFirstActivity = false;
                }
            }
        });
    }
}
