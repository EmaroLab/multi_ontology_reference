package it.emarolab.amor.owlInterface;

import com.google.common.base.Objects;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.HashSet;
import java.util.Set;

/**
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.DataPropertyRelations <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 *
 * <p>
 *     Class used to contain a data property relations associated to an individuals and its literal values.
 * </p>
 *
 * @version 2.1
 *
 */
public class DataPropertyRelations {
    private OWLDataProperty prop;
    private OWLNamedIndividual ind;
    private Set<OWLLiteral> value = new HashSet<>();

    /**
     * Fully construct this individual.
     * @param ind the individual having the described data properties.
     * @param prop the data properties that relate the individual with some values.
     * @param value the values of the data property.
     */
    public DataPropertyRelations(OWLNamedIndividual ind, OWLDataProperty prop, Set<OWLLiteral> value) {
        this.prop = prop;
        this.ind = ind;
        this.value = value;
    }

    /**
     * Fully construct this individual by name.
     * @param onto a reference to the ontology.
     * @param ind the name of individual having the described data properties.
     * @param prop the name of data properties that relate the individual with some values.
     * @param value the values of the data property. Type of data is specified by the type of {@code Object},
     *              see {@link OWLReferences#getOWLLiteral(Object)} for more.
     */
    public DataPropertyRelations(OWLReferences onto, String ind, String prop, Set<Object> value) {
        this.prop = onto.getOWLDataProperty( ind);
        this.ind = onto.getOWLIndividual( prop);
        for( Object o : value)
            this.value.add( onto.getOWLLiteral( o));
    }

    /**
     * Construct this individual without specify nether the property nor their values.
     * @param ind the individual having the described data properties.
     */
    public DataPropertyRelations(OWLNamedIndividual ind){
        this.ind = ind;
    }

    /**
     * Construct this individual by name, without specify nether the property nor their values.
     * @param onto a reference to the ontology.
     * @param ind the name of individual having the described data properties.
     */
    public DataPropertyRelations(OWLReferences onto, String ind) {
        this.prop = onto.getOWLDataProperty( ind);
    }

    /**
     * Construct this individual and property, without specify their values.
     * @param ind the individual having the described data properties.
     * @param prop the data properties that relate the individual with some values.
     */
    public DataPropertyRelations(OWLNamedIndividual ind, OWLDataProperty prop){
        this.ind = ind;
        this.prop = prop;
    }

    /**
     * Construct this individual and property by name, without specify their values.
     * @param onto a reference to the ontology.
     * @param ind the name of individual having the described data properties.
     * @param prop the name of data properties that relate the individual with some values.
     */
    public DataPropertyRelations(OWLReferences onto, String ind, String prop) {
        this.prop = onto.getOWLDataProperty( ind);
        this.ind = onto.getOWLIndividual( prop);
    }

    /**
     * @return the data property for this relations
     */
    public OWLDataProperty getProperty() {
        return prop;
    }

    /**
     * @return the individual having the {@link #getProperty()} with values {@link #getValues()}
     */
    public OWLNamedIndividual getIndividual() {
        return ind;
    }

    /**
     * @return the value of the {@link #getProperty()} for this relations
     */
    public Set<OWLLiteral> getValues() {
        return value;
    }

    /**
     * @return the data property name
     */
    public String getPropertyName() {
        return OWLReferences.getOWLName( prop);
    }

    /**
     * @return the individual name
     */
    public String getIndividualName() {
        return OWLReferences.getOWLName( ind);
    }

    /**
     * @return the values name
     */
    public Set<String> getValuesName() {
        return OWLReferences.getOWLName( value);
    }

    /**
     * Setter for the data property.
     * @param prop the data property to set.
     */
    public void setProperty(OWLDataProperty prop) {
        this.prop = prop;
    }

    /**
     * Setter for the individual having {@code this} relations.
     * @param ind the subject individual to set.
     */
    public void setIndividual(OWLNamedIndividual ind) {
        this.ind = ind;
    }

    /**
     * Setter for the values given tho the {@link #getIndividual()}
     * through {@link #getProperty()}.
     * @param value the value of the data property to set.
     */
    public void setValues(Set<OWLLiteral> value) {
        this.value = value;
    }

    /**
     * Setter for the values given tho the {@link #getIndividual()}
     * through {@link #getProperty()}. This clear previous values.
     * @param value the value of the data property to set.
     */
    public void setValues( OWLLiteral value) {
        this.value.clear();
        this.value.add( value);
    }

    /**
     * Another object is considered to be equal to this if they share the same:
     * {@link #getIndividual()} and {@link #getProperty()}.
     *
     * @param o an other object
     * @return {@code true} if {@code this == o}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataPropertyRelations)) return false;
        DataPropertyRelations that = (DataPropertyRelations) o;
        return Objects.equal(prop, that.prop) &&
                Objects.equal(ind, that.ind);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(prop, ind, value);
    }

    public String toString() {
        return "\"" + getIndividualName() + "." + getPropertyName() + "( " + getValuesName() + ")";
    }
}
