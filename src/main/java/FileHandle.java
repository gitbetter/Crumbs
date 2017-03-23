import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileHandle extends Object {
  private File file;
  private String contents = "";

  public FileHandle(String filename) throws IOException {
    this.file = new File(filename);
  }

  public FileHandle(File f) {
    this.file = f;
  }

  public String getFileContents() {
    if(this.contents == "") {
      this.contents = fileContents();
    }
    return this.contents;
  }

  public void setFileContents(String c) {
    this.contents = c;
  }

  public File getFile() {
    return this.file;
  }

  public void setFile(File f) {
    this.file = f;
  }

  public boolean isNew() {
    return !file.exists();
  }

  /**
	 *  Saves the current file to disk
	 *	@param f		The file to be saved
	 */
	public void saveToFile() {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
			writer.print(this.contents);
		} catch(IOException ex) {
			Utils.prl(ex.getMessage());
		}
	}

	/**
	 *  Load the contents of an existing file
	 *  onto the editor
	 *  @param f		The file whose contents are to be loaded
	 */
	public String fileContents() {
    String fileContents = "";

    if(isNew()) return fileContents;

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = reader.readLine()) != null) {
				fileContents += line + "\n";
			}
		} catch(IOException ex) {
			Utils.prl(ex.getMessage());
		}

    return fileContents;
	}

  @Override
  public int hashCode() {
    int hash = 17;
    hash = 37 * hash + this.file.hashCode();

    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if(o != null && o instanceof FileHandle) {
      FileHandle object = (FileHandle) o;
      if(object.getFile().getAbsolutePath().equals(this.file.getAbsolutePath()))
        return true;
    }

    return false;
  }
}
