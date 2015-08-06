//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.05 at 12:34:10 AM BST 
//


package pl.baczkowicz.mqttspy.common.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.CopyStrategy;
import org.jvnet.jaxb2_commons.lang.CopyTo;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy;
import org.jvnet.jaxb2_commons.lang.HashCode;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * <p>Java class for Formatting complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Formatting"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Formatter" type="{http://baczkowicz.pl/mqtt-spy/common}FormatterDetails" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Formatting", propOrder = {
    "formatter"
})
public class Formatting implements Cloneable, CopyTo, Equals, HashCode, ToString
{

    @XmlElement(name = "Formatter")
    protected List<FormatterDetails> formatter;

    /**
     * Default no-arg constructor
     * 
     */
    public Formatting() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Formatting(final List<FormatterDetails> formatter) {
        this.formatter = formatter;
    }

    /**
     * Gets the value of the formatter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formatter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormatter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FormatterDetails }
     * 
     * 
     */
    public List<FormatterDetails> getFormatter() {
        if (formatter == null) {
            formatter = new ArrayList<FormatterDetails>();
        }
        return this.formatter;
    }

    public String toString() {
        final ToStringStrategy strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        {
            List<FormatterDetails> theFormatter;
            theFormatter = (((this.formatter!= null)&&(!this.formatter.isEmpty()))?this.getFormatter():null);
            strategy.appendField(locator, this, "formatter", buffer, theFormatter);
        }
        return buffer;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if (!(object instanceof Formatting)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final Formatting that = ((Formatting) object);
        {
            List<FormatterDetails> lhsFormatter;
            lhsFormatter = (((this.formatter!= null)&&(!this.formatter.isEmpty()))?this.getFormatter():null);
            List<FormatterDetails> rhsFormatter;
            rhsFormatter = (((that.formatter!= null)&&(!that.formatter.isEmpty()))?that.getFormatter():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "formatter", lhsFormatter), LocatorUtils.property(thatLocator, "formatter", rhsFormatter), lhsFormatter, rhsFormatter)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy strategy) {
        int currentHashCode = 1;
        {
            List<FormatterDetails> theFormatter;
            theFormatter = (((this.formatter!= null)&&(!this.formatter.isEmpty()))?this.getFormatter():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "formatter", theFormatter), currentHashCode, theFormatter);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

    public Object clone() {
        return copyTo(createNewInstance());
    }

    public Object copyTo(Object target) {
        final CopyStrategy strategy = JAXBCopyStrategy.INSTANCE;
        return copyTo(null, target, strategy);
    }

    public Object copyTo(ObjectLocator locator, Object target, CopyStrategy strategy) {
        final Object draftCopy = ((target == null)?createNewInstance():target);
        if (draftCopy instanceof Formatting) {
            final Formatting copy = ((Formatting) draftCopy);
            if ((this.formatter!= null)&&(!this.formatter.isEmpty())) {
                List<FormatterDetails> sourceFormatter;
                sourceFormatter = (((this.formatter!= null)&&(!this.formatter.isEmpty()))?this.getFormatter():null);
                @SuppressWarnings("unchecked")
                List<FormatterDetails> copyFormatter = ((List<FormatterDetails> ) strategy.copy(LocatorUtils.property(locator, "formatter", sourceFormatter), sourceFormatter));
                copy.formatter = null;
                if (copyFormatter!= null) {
                    List<FormatterDetails> uniqueFormatterl = copy.getFormatter();
                    uniqueFormatterl.addAll(copyFormatter);
                }
            } else {
                copy.formatter = null;
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new Formatting();
    }

}
