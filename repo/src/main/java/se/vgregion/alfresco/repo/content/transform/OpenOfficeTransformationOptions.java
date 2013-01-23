/**
 *
 */
package se.vgregion.alfresco.repo.content.transform;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;

/**
 * @author <a href="mailto:axel.faust@prodyna.com">Axel Faust</a>, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public class OpenOfficeTransformationOptions extends TransformationOptions
{
    /*
     * PDF export options
     */

    public static final String OPT_PDF_VERSION = "SelectPdfVersion";
    public static final String OPT_USE_LOSSLESS_COMPRESSION = "UseLosslessCompression";
    public static final String OPT_EXPORT_NOTES = "ExportNotes";
    public static final String OPT_EXPORT_NOTES_PAGES = "ExportNotesPages";

    public static final String[] PDF_OPTIONS = { OPT_PDF_VERSION, OPT_USE_LOSSLESS_COMPRESSION, OPT_EXPORT_NOTES, OPT_EXPORT_NOTES_PAGES };

    // currently only PDF, but may be enhanced with more in the future
    public static final String[] ALL_OPTIONS = PDF_OPTIONS;

    /** The PDF version to export to - 0 for PDF 1.4, 1 for PDF/A-1 */
    private Integer pdfVersion;

    /** Wether to use lossless compression images (PNG) */
    private Boolean useLosslessCompression;

    /** Wether to export notes */
    private Boolean exportNotes;

    /** Wether to export notes pages */
    private Boolean exportNotesPages;

    /**
     * Default constructor
     */
    public OpenOfficeTransformationOptions()
    {
    }

    /**
     * Constructor
     *
     * @param sourceNodeRef
     *            the source node reference
     * @param sourceContentProperty
     *            the source content property
     * @param targetNodeRef
     *            the target node reference
     * @param targetContentProperty
     *            the target content property
     */
    public OpenOfficeTransformationOptions(final NodeRef sourceNodeRef, final QName sourceContentProperty, final NodeRef targetNodeRef,
            final QName targetContentProperty)
    {
        super(sourceNodeRef, sourceContentProperty, targetNodeRef, targetContentProperty);
    }

    /**
     * Constructor. Creates a transformation options object from a map. Provided for back ward compatibility.
     *
     * @param optionsMap
     *            options map
     */
    public OpenOfficeTransformationOptions(final Map<String, Object> optionsMap)
    {
        super(optionsMap);
        this.pdfVersion = (Integer)optionsMap.get(OPT_PDF_VERSION);
        this.useLosslessCompression = (Boolean)optionsMap.get(OPT_USE_LOSSLESS_COMPRESSION);
        this.exportNotes = (Boolean)optionsMap.get(OPT_EXPORT_NOTES);
        this.exportNotesPages = (Boolean)optionsMap.get(OPT_EXPORT_NOTES_PAGES);
    }

    /**
     * @return the pdfVersion
     */
    public Integer getPdfVersion()
    {
        return pdfVersion;
    }

    /**
     * Sets the PDF version to export to
     *
     * @param pdfVersion
     *            0 for PDF 1.4 and 1 for PDF/A-1
     */
    public void setPdfVersion(final Integer pdfVersion)
    {
        this.pdfVersion = pdfVersion;
    }

    /**
     * @return the useLosslessCompression
     */
    public Boolean getUseLosslessCompression()
    {
        return useLosslessCompression;
    }

    /**
     * Specifies wether lossless compression images (PNG) should be used in the exported PDF document
     *
     * @param useLosslessCompression
     *            {@code true} if PNG are to be used, {@code false} otherwise
     */
    public void setUseLosslessCompression(final Boolean useLosslessCompression)
    {
        this.useLosslessCompression = useLosslessCompression;
    }

    /**
     * @return the exportNotes
     */
    public Boolean getExportNotes()
    {
        return exportNotes;
    }

    /**
     * Specifies wether notes (review comments, e.g. in Word-like documents) should be included in the transformation / export
     *
     * @param exportNotes
     *            {@code true} if notes should be exported, {@code false} otherwise
     */
    public void setExportNotes(final Boolean exportNotes)
    {
        this.exportNotes = exportNotes;
    }

    /**
     * @return the exportNotesPages
     */
    public Boolean getExportNotesPages()
    {
        return exportNotesPages;
    }

    /**
     * Specifies wether notes pages (of presentations) should be included in the transformation / export
     *
     * @param exportNotesPages
     *            {@code true} if notes pages should be exported, {@code false} otherwise
     */
    public void setExportNotesPages(final Boolean exportNotesPages)
    {
        this.exportNotesPages = exportNotesPages;
    }

    /**
     * Convert the transformation options into a map.
     * <p>
     * OpenOffice specific options (optional) are:
     * <ul>
     *   <li>PDF transformation options</li>
     *   <ul>
     *     <li>{@link #OPT_PDF_VERSION}</li>
     *     <li>{@link #OPT_USE_LOSSLESS_COMPRESSION}</li>
     *     <li>{@link #OPT_EXPORT_NOTES}</li>
     *     <li>{@link #OPT_EXPORT_NOTES_PAGES}</li>
     *   </ul>
     * </ul>
     * <p>
     * Override this method to append option values to the map.  Derived classes should call
     * the base class before appending further values and returning the result.
     */
    @Override
    public Map<String, Object> toMap()
    {
        final Map<String, Object> optionsMap = super.toMap();
        optionsMap.put(OPT_PDF_VERSION, pdfVersion);
        optionsMap.put(OPT_USE_LOSSLESS_COMPRESSION, useLosslessCompression);
        optionsMap.put(OPT_EXPORT_NOTES, exportNotes);
        optionsMap.put(OPT_EXPORT_NOTES_PAGES, exportNotesPages);
        return optionsMap;
    }
}
