package com.teragrep.nbs_01.repository;

import jakarta.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a single Notebook within Zeppelin
 */
public final class Notebook implements ZeppelinFile {

  private static final Logger LOGGER = LoggerFactory.getLogger(Notebook.class);
  private final LinkedHashMap<String,Paragraph> paragraphs;
  private final String title;
  private final String id;
  private final Path path;
  public Notebook(String title, String id, Path path, LinkedHashMap<String,Paragraph> paragraphs) {
    this.id = id;
    this.path = path;
    this.title = title;
    this.paragraphs = paragraphs;
  }

  // Remove file from disk
  @Override
  public void delete() throws IOException {
    Files.delete(path());
  }

  // Checks if searched ID matches with this Notebooks ID.
  @Override
  public ZeppelinFile findFile(String id) throws FileNotFoundException {
    if(id().equals(id)){
      return this;
    }
    else {
      throw new FileNotFoundException("Searched id "+id+" does not match with"+id());
    }
  }

  // Checks if searched path matches with this Notebooks path.
  @Override
  public ZeppelinFile findFile(Path path) throws FileNotFoundException {
    if(path().equals(path)){
      return this;
    }
    else {
      throw new FileNotFoundException("Searched path "+path+" does not match with"+path());
    }
  }
  public String id() {
    return id;
  }

  public Path path() {
    return path;
  }

  public String title() {
    return title;
  }
  public LinkedHashMap<String,Paragraph> paragraphs(){
    return paragraphs;
  }

  public JsonObject json(){
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("id",id());
    builder.add("name", title);
    //compatibility fields//
    builder.add("config",Json.createObjectBuilder(new HashMap<>()).build());
    // end //
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    for (Paragraph paragraph:paragraphs.values()
    ) {
      arrayBuilder.add(paragraph.json());
    }
    JsonArray paragraphJsonArray = arrayBuilder.build();
    builder.add("paragraphs",paragraphJsonArray);
    return builder.build();
  }
  public Notebook copy(Path path, String id) throws IOException {
    return copy(title,path,id);
  }

  @Override
  public Map<String, ZeppelinFile> children() {
    return new HashMap<>();
  }


  @Override
  public void printTree() {
    System.out.println("File ID: "+id()+", Path: "+path());
    LOGGER.debug("File ID: {}, Path: {}",id(),path());
  }

  @Override
  public Notebook load() throws IOException {
    String content = readFile().toString();
    StringReader stringReader = new StringReader(content);
    JsonReader jsonReader = Json.createReader(stringReader);
    JsonObject object = jsonReader.readObject();
    jsonReader.close();
    stringReader.close();

    String name = object.getString("name");
    String id = object.getString("id");
    JsonArray paragraphJsonArray = object.getJsonArray("paragraphs");
    LinkedHashMap<String, Paragraph> paragraphs = new LinkedHashMap<>();
    for (JsonObject paragraphJson:paragraphJsonArray.getValuesAs(JsonObject.class)
    ) {
      NullParagraph nullParagraph = new NullParagraph();
      Paragraph paragraph = nullParagraph.fromJson(paragraphJson);
      paragraphs.put(paragraph.id(),paragraph);
    }
    return new Notebook(name,id,path(),paragraphs);
  }

  @Override
  public List<ZeppelinFile> listAllChildren() {
    return new ArrayList<>();
  }

  public Notebook copy(String title, Path path, String id) throws IOException {
    LinkedHashMap<String, Paragraph> copyParagraphs = new LinkedHashMap<String,Paragraph>();
    for (Paragraph paragraph: paragraphs.values()) {
      String copyParagraphId = UUID.randomUUID().toString();
      Script copyScript = new Script(paragraph.script().text());
      Paragraph copyParagraph = new Paragraph(copyParagraphId,paragraph.title(),copyScript);
      copyParagraphs.put(copyParagraph.id(),copyParagraph);
    }
    Notebook copyNotebook = new Notebook(title,id,path,copyParagraphs);
    copyNotebook.save();
    return copyNotebook;
  }

  public void save() throws IOException {
    try{
      StringWriter stringWriter = new StringWriter();
      Files.createDirectories(path().getParent());
      Files.write(path(), json().toString().getBytes());
      stringWriter.close();
    }
    catch (IOException exception){
      throw new IOException("Failed to save notebook to path"+path()+"!",exception);
    }
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  public void rename(String fileName) throws IOException {
    move(Paths.get(path().getParent().toString(),fileName));
  }

  public void move(Path path) throws IOException {
    Notebook movedNotebook = copy(path,id());
    movedNotebook.save();
    delete();
  }
  public Notebook move(Directory parentDirectory) throws IOException {
    return move(parentDirectory,path().getFileName().toString());
  }
  public Notebook move(Directory parentDirectory, String fileName) throws IOException {
    Notebook movedNotebook = copy(Paths.get(parentDirectory.path().toString(),fileName),id());
    movedNotebook.save();
    delete();
    return movedNotebook;
  }

  public boolean isStub(){
    return false;
  }
  private String readFile() throws IOException {
    List<String> lines = Files.readAllLines(path(), Charset.defaultCharset());
    String concatenatedLines = lines.stream()
            .map(n -> String.valueOf(n))
            .collect(Collectors.joining("\n"));
    return concatenatedLines;
  }
}
