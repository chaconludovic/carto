package com.cartefa.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import dk.lindhardt.gwt.geie.server.CSV2TableLayout;
import dk.lindhardt.gwt.geie.server.Excel2TableLayout;
import dk.lindhardt.gwt.geie.shared.Cell;
import dk.lindhardt.gwt.geie.shared.TableLayout;

/**
 * User: AnAmuser Date: 01-06-11
 * <p/>
 * Handles importing of excel files
 */
public class Import extends HttpServlet {

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			ServletFileUpload upload = new ServletFileUpload();
			response.setContentType("text/html");

			TableLayout tableLayout = null;
			String type = request.getParameter("type");
			String result = "";
			FileItemIterator iterator = upload.getItemIterator(request);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				if (!item.isFormField()) {
					InputStream stream = item.openStream();
					// Parse stream to TableLayout
					if (type.equals("xls")) {
						WorkbookSettings ws = new WorkbookSettings();
						ws.setLocale(new Locale("en"));
						Workbook workbook = Workbook.getWorkbook(stream, ws);
						Excel2TableLayout excelParser = new Excel2TableLayout(
								workbook);
						tableLayout = excelParser.build(0);
					} else if (type.equals("csv")) {
						/*
						 * // Must be called before the iterator.hasNext() is //
						 * called again CSV2TableLayout csvParser = new
						 * CSV2TableLayout(stream); tableLayout =
						 * csvParser.build(); int nbreLignes =
						 * tableLayout.rows(); // for (int
						 * i=0;i<nbreLignes;i++){ // tableLayout. // } for (Cell
						 * cell : tableLayout.getCells()) { result +=
						 * cell.getValue() + "echap"; }
						 */
						String csvString = convertStreamToString(stream);
						List<List<String>> lines = getCsvStructure(csvString);
						for (List<String> ligne : lines) {
							for (String cell : ligne) {
								result += cell + ";";
							}
							result += "echap";
						}
					}
				}
				if (tableLayout != null) {
					break;
				}
			}

			// // Serialize table
			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// ObjectOutputStream out = new ObjectOutputStream(bos);
			// out.writeObject(tableLayout);
			// out.close();
			// // Persist it in BlobStore
			// FileService fileService = FileServiceFactory.getFileService();
			// AppEngineFile file = fileService
			// .createNewBlobFile("application/x-java-serialized-object");
			// FileWriteChannel writeChannel =
			// fileService.openWriteChannel(file,
			// true);
			// writeChannel.write(ByteBuffer.wrap(bos.toByteArray()));
			// writeChannel.closeFinally();
			// bos.close();
			//
			// // Get blobkey
			// BlobKey blobKey = fileService.getBlobKey(file);
			// response.getWriter().print(blobKey.getKeyString());

			response.getWriter().print(result);
		} catch (Exception ex) {
			throw new ServletException("Some error happened", ex);
		}

	}

	private String separator = ";";
	private String charset = "utf-8";
	private String newLine = System.getProperty("line.separator");

	public String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						charset));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	private List<List<String>> getCsvStructure(String csvString) {
		List<List<String>> csvStructure = new ArrayList<List<String>>();
		String[] lines = csvString.split(newLine);
		Pattern csvPattern = Pattern.compile("((?:[^\"" + separator
				+ "]|(?:\"(?:\\\\{2}|\\\\\"|[^\"])*?\"))*)");
		for (String line : lines) {
			List<String> cells = new ArrayList<String>();
			Matcher matcher = csvPattern.matcher(line);

			boolean useNextText = true;
			while (matcher.find()) {
				String cell = matcher.group(1);
				// The following logic is just needed because it is a fucked
				// pattern, but it's easier than write a working pattern.
				if (cell != null && cell.length() == 0 && !useNextText) {
					useNextText = true;
				} else if (cell != null && cell.length() == 0 && useNextText) {
					useNextText = true;
					cells.add("");
				} else {
					// Remove quotes
					cell = cell.trim();
					if (cell.startsWith("\"") && cell.endsWith("\"")) {
						cell = cell.substring(1, cell.length() - 1);
					}
					cell = cell.replaceAll("\"\"", "\"");
					cells.add(cell);

					useNextText = false;
				}
			}
			csvStructure.add(cells);
		}
		return csvStructure;
	}

}
