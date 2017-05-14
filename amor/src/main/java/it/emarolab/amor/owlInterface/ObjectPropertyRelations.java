package it.emarolab.amor.owlInterface;

import com.google.common.base.Objects;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.ObjectPropertyRelations <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 *
 * <p>
 *     Class used to contain an object property relation associated to an individuals and its value entities.
 * </p>
 *
 * @version 2.1
 *
 */
public class ObjectPropertyRelations {
    private OWLObjectProperty prop;
    private OWLNamedIndividual ind;
    private Set<OWLNamedIndividual> value = new HashSet<>();

    /**
     * Fully construct this individual.
     * @param ind the individual having the described data properties.
     * @param prop the object properties that relate the individual with some values.
     * @param value the values of the object property.
     */
    public ObjectPropertyRelations(OWLNamedIndividual ind, OWLObjectProperty prop, Set<OWLNamedIndividual> value) {
        this.prop = prop;
        this.ind = ind;
        this.value = value;
    }

    /**
     * Fully construct this individual by name.
     * @param onto a reference to the ontology.
     * @param ind the name of individual having the described object properties.
     * @param prop the name of object properties that relate the individual with some values.
     * @param value the values of the object property.
     */
    public ObjectPropertyRelations(OWLReferences onto, String ind, String prop, Set<String> value) {
        this.prop = onto.getOWLObjectProperty( ind);
        this.ind = onto.getOWLIndividual( prop);
        for( String o : value)
            this.value.add( onto.getOWLIndividual( o));
    }

    /**
     * Construct this individual without specify nether the property nor their values.
     * @param ind the individual having the described data properties.
     */
    public ObjectPropertyRelations(OWLNamedIndividual ind){
        this.ind = ind;
    }

    /**
     * Construct this individual by name, without specify nether the property nor their values.
     * @param onto a reference to the ontology.
     * @param ind the name of individual having the described data properties.
     */
    public ObjectPropertyRelations(OWLReferences onto, String ind) {
        this.prop = onto.getOWLObjectProperty( ind);
    }

    /**
     * Construct this individual and property without specify their values.
     * @param ind the individual having the described data properties.
     * @param prop the data properties that relate the individual with some values.
     */
    public ObjectPropertyRelations(OWLNamedIndividual ind, OWLObjectProperty prop){
        this.ind = ind;
        this.prop = prop;
    }

    /**
     * Construct this individual and property by name, without specify their values.
     * @param onto a reference to the ontology.
     * @param ind the name of individual having the described data properties.
     * @param prop the name of data properties that relate the individual with some values.
     */
    public ObjectPropertyRelations(OWLReferences onto, String ind, String prop) {
        this.prop = onto.getOWLObjectProperty( ind);
        this.ind = onto.getOWLIndividual( prop);
    }

    /**
     * Setter for the object property.
     * @param prop the object property to set.
     */
    public void setProperty(OWLObjectProperty prop) {
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
     * @param value the value of the object property to set.
     */
    public void setValue(Set<OWLNamedIndividual> value) {
        this.value = value;
    }

    /**
     * @return the object property for this relations
     */
    public OWLObjectProperty getProperty() {
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
    public Set<OWLNamedIndividual> getValues() {
        return value;
    }

    /**
     * @return the object property name
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
     * Another object is considered to be equal to this if they share the same:
     * {@link #getIndividual()} and {@link #getProperty()}.
     *
     * @param o an other object
     * @return {@code true} if {@code this == o}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectPropertyRelations)) return false;
        ObjectPropertyRelations that = (ObjectPropertyRelations) o;
        return Objects.equal(prop, that.prop) &&
                Objects.equal(ind, that.ind);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(prop, ind);
    }

    public String toString() {
        return "\"" + getIndividualName() + "." + getPropertyName() + "( " + getValuesName() + ")";
    }
}
