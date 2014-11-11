package org.xmlcml.xhtml2stm.visitor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xmlcml.svg2xml.container.AbstractContainer;
import org.xmlcml.xhtml2stm.result.ResultList;
import org.xmlcml.xhtml2stm.result.ResultsElement;
import org.xmlcml.xhtml2stm.visitable.AbstractVisitable;
import org.xmlcml.xhtml2stm.visitable.SourceElement;
import org.xmlcml.xhtml2stm.visitable.VisitableContainer;
import org.xmlcml.xhtml2stm.visitable.VisitableInput;
import org.xmlcml.xhtml2stm.visitable.html.HtmlContainer;
import org.xmlcml.xhtml2stm.visitable.html.HtmlVisitable;
import org.xmlcml.xhtml2stm.visitable.image.ImageContainer;
import org.xmlcml.xhtml2stm.visitable.image.ImageVisitable;
import org.xmlcml.xhtml2stm.visitable.pdf.PDFContainer;
import org.xmlcml.xhtml2stm.visitable.pdf.PDFVisitable;
import org.xmlcml.xhtml2stm.visitable.svg.SVGContainer;
import org.xmlcml.xhtml2stm.visitable.svg.SVGVisitable;
import org.xmlcml.xhtml2stm.visitable.table.TableVisitable;
import org.xmlcml.xhtml2stm.visitable.xml.XMLContainer;
import org.xmlcml.xhtml2stm.visitable.xml.XMLVisitable;

/** visits the visitables (data).
 * 
 * Normally the visitation is to carry out search.
 * Search is delegated to a searcher.
 * 
 * @author pm286
 *
 */
public abstract class AbstractVisitor {

	private final static Logger LOG = Logger.getLogger(AbstractVisitable.class);

	public static final String RESULTS_XML = "results.xml";
	private static final String XML = "xml";
	private static final String HTM = "htm";

	protected ResultsElement resultsElement;
	protected SourceElement sourceElement;
	private VisitableInput visitableInput;
	private VisitorOutput visitorOutput;
	protected AbstractVisitable currentVisitable;
	private XPathProcessor xPathProcessor;

	// ============== VISITATION ========================
	
	public void visit(AbstractVisitable visitable) {
		// we seem to have to subclass to achieve double dispatch
		ensureResultsElement();
		visitSubclassed(visitable);
		currentVisitable = visitable;
	}

	/** The doVisit(Visitable) routines should be overridden in any subclass
	 * which wants to use them. This is normally through a stub of the form:
	 * 
	 * 	public void visit(HtmlVisitable htmlVisitable) {
	 * 		doVisit(htmlVisitable);
	 *  }
	 */
	
	/**
	 * HTML
	 * 
	 * Override as above if visitor can process HTML
	 * 
	 * @param htmlVisitable
	 */

	public void visit(HtmlVisitable htmlVisitable) {
		notYetImplemented(htmlVisitable);
	}
	
	protected final void doVisit(HtmlVisitable htmlVisitable) {
		List<HtmlContainer> htmlContainerList = htmlVisitable.getHtmlContainerList();
		for (HtmlContainer htmlContainer : htmlContainerList) {
			doSearchAndAddResults(htmlContainer);
		}
	}

	private void doSearchAndAddResults(VisitableContainer container) {
		LOG.debug("doSearchAndAddResults "+container.getClass());
		AbstractSearcher searcher = createSearcher();
		searcher.search(container);
		ensureResultsElement();
		resultsElement.appendChild(searcher.getResultsElement());
	}

	/**
	 * XML
	 * 
	 * Override as above if visitor can process XML
	 * 
	 * @param xmlVisitable
	 */

	public void visit(XMLVisitable xmlVisitable) {
		notYetImplemented(xmlVisitable);
	}
	
	protected final void doVisit(XMLVisitable xmlVisitable) {
		for (XMLContainer xmlContainer : xmlVisitable.getXMLContainerList()) {
			doSearchAndAddResults(xmlContainer);
		}
	}

	/**
	 * SVG
	 * 
	 * Override as above if visitor can process SVG
	 * 
	 * @param svgVisitable
	 */

	public void visit(SVGVisitable svgVisitable) {
		notYetImplemented(svgVisitable);
	}
	
	protected final void doVisit(SVGVisitable svgVisitable) {
		for (SVGContainer svgContainer : svgVisitable.getSVGContainerList()) {
			doSearchAndAddResults(svgContainer);
		}
	}

	/**
	 * Image
	 * 
	 * Override as above if visitor can process Image
	 * 
	 * @param imageVisitable
	 */

	public void visit(ImageVisitable imageVisitable) {
		notYetImplemented(imageVisitable);
	}

	protected final void doVisit(ImageVisitable imageVisitable) {
		for (ImageContainer imageContainer : imageVisitable.getImageContainerList()) {
			doSearchAndAddResults(imageContainer);
		}
	}


	/**
	 * PDF
	 * 
	 * @param pdfVisitable
	 */
	public void visit(PDFVisitable pdfVisitable) {
		notYetImplemented(pdfVisitable);
	}

	/** This may get altered to reflect PDF components.
	 * 
	 * @param pdfVisitable
	 */
	protected final void doVisit(PDFVisitable pdfVisitable) {
		for (PDFContainer pdfContainer : pdfVisitable.getPDFContainerList()) {
			doSearchAndAddResults(pdfContainer);
		}
	}

	/**
	 * Table
	 * 
	 * Not yet working - may get redesigned
	 * 
	 * @param tableVisitable
	 */
	public void visit(TableVisitable tableVisitable) {
		notYetImplemented(tableVisitable);
	}
	
	private void visitSubclassed(AbstractVisitable visitable) {
		if (visitable instanceof HtmlVisitable) {
			this.visit((HtmlVisitable) visitable);
		} else if (visitable instanceof ImageVisitable) {
			this.visit((ImageVisitable) visitable);
		} else if (visitable instanceof XMLVisitable) {
			this.visit((XMLVisitable) visitable);
		} else if (visitable instanceof PDFVisitable) {
			this.visit((PDFVisitable) visitable);
		} else if (visitable instanceof SVGVisitable) {
			this.visit((SVGVisitable) visitable);
		} else if (visitable instanceof TableVisitable) {
			this.visit((TableVisitable) visitable);
		} else {
			throw new RuntimeException("Unknown visitable: " + visitable);
		}
	}

	protected void notApplicable(AbstractVisitable visitable) {
		throw new RuntimeException(this.getClass().getName()
				+ " cannot be applied to " + visitable);
	}

	protected void notYetImplemented(AbstractVisitable visitable) {
		throw new RuntimeException(this.getClass().getName()
				+ " is not yet applicable to " + visitable.getClass().getName()+"; perhaps add doVisit() to visitor?");
	}

	protected String getDescription() {
		return "Visitor, often invoked by using specific command (e.g.'species') \n"+
			   "Command line of form: '[command] [options], command being 'species', 'sequence', etc.\n";

	}

	// ============== ARGUMENTS ================
	
	protected void usage() {
		System.err.println(getDescription());
		additionalUsage(); // for specific visitors
		System.err
				.println("Universal options ('-f' is short for '--foo', etc.):");
		System.err.println("    -i  --input  inputSpec");
		System.err
				.println("                 mandatory: filename, directoryName, url, or (coming RSN) identifier (e.g. PMID:12345678)");
		System.err.println("    -o  --output  outputSpec");
		System.err
				.println("                 mandatory: filename, directoryName");
		System.err.println("    -r  --recursive");
		System.err.println("                 recurse through directories");
		System.err.println("    -e  --extensions ext1 [ext2 ...]");
		System.err
				.println("                 mandatory if input directory: file extensions (htm, pdf, etc.)");
		System.err.println("");
	}

	protected void additionalUsage() {

	}

	private void runArgProcessor(String[] commandLineArgs) {
		ArgProcessor argProcessor = new ArgProcessor(commandLineArgs, this);
		
		createVisitableInputFromArgs(argProcessor);
		createVisitableOutputFromArgs();
		
		visitVistablesAndWriteOutputFiles();
	}

	private void createVisitableInputFromArgs(ArgProcessor argProcessor) {
		visitableInput = argProcessor.getVisitableInput();
		setVisitorOutput(argProcessor.getVisitorOutput());
		setXPathProcessor(argProcessor.getXPathProcessor());
		if (visitableInput == null) {
			throw new RuntimeException("input option mandatory");
		}
		visitableInput.setExtensions(argProcessor.getExtensions());
		visitableInput.setRecursive(argProcessor.isRecursive());
		visitableInput.createVisitableList();
		LOG.debug("visitable list: "+visitableInput.getVisitableList());
		LOG.debug("in: " + visitableInput);
	}

	private void createVisitableOutputFromArgs() {
		if (getOrCreateVisitorOutput() == null) {
			setVisitorOutput(new VisitorOutput());
		}
		getOrCreateVisitorOutput().setVisitableInput(visitableInput);
		
		LOG.debug("outputLocation: " + getOrCreateVisitorOutput().getOutputLocation());
		getOrCreateVisitorOutput().setExtension(XML);
	}

	private void setXPathProcessor(XPathProcessor xPathProcessor) {
		this.xPathProcessor = xPathProcessor;
	}
	
	protected XPathProcessor getXPathProcessor() {
		return xPathProcessor;
	}

	/** Normal argument processing.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void processArgs(String[] args) throws Exception {
		if (args != null && args.length > 0) {
			runArgProcessor(args);
		} else {
			usage();
		}
	}

	/**
	 * Overriden by subclasses which can have extra arguments.
	 * 
	 * @param arg immediate arg after "-foo" flag
	 * @param listIterator multiple argument values
	 */
	protected boolean processArg(String arg, ListIterator<String> listIterator) {
		return false;
	}
	
	// =============== SEARCH ===============

	/** searches the visitable container.
	 * 
	 * Wraps container in a SourceElement to carry metadata and context.
	 * 
	 * precise search is delegated to subclasses.
	 * 
	 * output is wrapped in resultsElement.
	 * 
	 * @param container
	 */
	public void searchContainer(VisitableContainer container) {
		AbstractSearcher searcher = AbstractSearcher.createDefaultSearcher(this);
		ensureResultsElement();
		SourceElement sourceElement = new SourceElement(container);
//		resultsElement.appendChild(sourceElement); // need to capture metadata somehow
		ResultList resultList = searcher.searchXPathPatternAndCollectResults(sourceElement);
		resultsElement.appendChild(searcher.createListElement(resultList));
	}

	// ================= PDF search (needs rewriting) ==========
	/**
	 * Turns PDF to HTML and searches that.
	 * // FIXME need a better way 
	 * @param pdfVisitable
	 */
	protected void createAndSearchHtmlContainer(PDFVisitable pdfVisitable) {
		List<PDFContainer> pdfContainerList = pdfVisitable.getPDFContainerList();
		for (PDFContainer pdfContainer : pdfContainerList) {
			HtmlContainer htmlContainer = pdfContainer.getHtmlContainer();
			if (htmlContainer != null) {
				searchContainer(htmlContainer);
			}
		}
	}

	/**
	 * Turns PDF to SVG and searches that.
	 * 
	 * @param pdfVisitable
	 */
	protected void createAndSearchSVGContainer(PDFVisitable pdfVisitable) {
		List<PDFContainer> pdfContainerList = pdfVisitable.getPDFContainerList();
		for (PDFContainer pdfContainer : pdfContainerList) {
			List<SVGContainer> svgContainerList = pdfContainer.getSVGListContainer();
			if (svgContainerList != null) {
				searchContainer(svgContainerList);
			}
		}
	}

	protected void searchContainer(List<SVGContainer> svgContainerList) {throw new RuntimeException("Must override");}

	// =============== RESULTS =======================
	
	protected void ensureResultsElement() {
		if (resultsElement == null) {
			resultsElement = new ResultsElement();
		}
	}

	public ResultsElement getResultsElement() {
		ensureResultsElement();
		return resultsElement;
	}

	// OUTPUT ======================================

	public VisitorOutput getOrCreateVisitorOutput() {
		if (visitorOutput == null) {
			visitorOutput = new VisitorOutput();
		}
		return visitorOutput;
	}

	public void setVisitorOutput(VisitorOutput visitorOutput) {
		this.visitorOutput = visitorOutput;
	}

	private void visitVistablesAndWriteOutputFiles() {
		List<AbstractVisitable> inputVisitableList = visitableInput.getVisitableList();
		if (inputVisitableList.size() == 0) {
			LOG.error("No visitable input list");
		} else {
			LOG.debug("InputVisitables " + inputVisitableList.size());
			for (AbstractVisitable visitable : inputVisitableList) {
				LOG.debug("input file List "+ visitable.getFileList().size());
				visit(visitable);
				createAndWriteOutputFiles();
			}
		}
	}

	private void createAndWriteOutputFiles() {
		File outputDir = getOrCreateVisitorOutput().getOutputDirectoryFile();
		File resultsFile = new File(outputDir, RESULTS_XML);
		List<File> files = currentVisitable.getFileList();
		if (resultsElement == null) {
			LOG.error("***WARNING results element is null");
		} else if (files.size() <= 1) {  // why not <= 0??
			LOG.error("no visitableOutput fileList");
			LOG.debug("creating output file "+resultsFile+" // "+resultsElement.toXML());
			writeFile(resultsElement.toXML(), resultsFile);
		} else {
			LOG.error("visitableOutput fileList "+files.size());
			for (File file : files) {
				LOG.debug("making directory from file: " + file);
				File ff = writeFile(outputDir, "about.txt", "created");
				if (resultsElement == null) {
					LOG.error("no results to write");
				} else {
					writeFile(outputDir, file.toString(), resultsElement.toXML());
					LOG.debug("ACTUALLY writing to: " + resultsFile);
				}
			}
		}
	}

	private File writeFile(File newDir, String filename, String text) {
		File file = new File(newDir, filename);
		writeFile(text, file);
		return file;
	}

	private void writeFile(String text, File ff) {
		try {
			FileUtils.writeStringToFile(ff, text);
		} catch (IOException e) {
			LOG.error("Cannot create file: " + ff);
		}
	}

	/** Visitor-specific search tool.
	 * 
	 * @return
	 */
	protected abstract AbstractSearcher createSearcher();

	// ==============================================
}
