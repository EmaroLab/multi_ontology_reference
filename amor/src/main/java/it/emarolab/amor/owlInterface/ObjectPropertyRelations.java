package it.emarolab.amor.owlInterface;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

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
 *     A {@link ObjectPropertyRelations} object is returned by
 *     {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
 *     and {@link OWLEnquirer#getObjectPropertyB2Individual(String)}.
 * </p>
 *
 * @version 2.1
 *
 */
public class ObjectPropertyRelations {
    private OWLObjectProperty prop;
    private OWLNamedIndividual ind;
    private Set<OWLNamedIndividual> value;
    private String propName, indName;
    private Set<String> valueName;

    public ObjectPropertyRelations(OWLNamedIndividual ind, OWLObjectProperty prop, Set<OWLNamedIndividual> value,
                                   OWLReferencesInterface ontoRef) {
        this.prop = prop;
        this.propName = ontoRef.getOWLObjectName(prop);
        this.ind = ind;
        this.indName = ontoRef.getOWLObjectName(ind);
        this.value = value;
        this.valueName = ontoRef.getOWLObjectName(value);
    }

    public OWLObjectProperty getProperty() {
        return prop;
    }

    public OWLNamedIndividual getIndividual() {
        return ind;
    }

    public Set<OWLNamedIndividual> getValues() {
        return value;
    }

    public String getPropertyName() {
        return propName;
    }

    public String getIndividualName() {
        return indName;
    }

    public Set<String> getValuesName() {
        return valueName;
    }

    public String toString() {
        return "\"" + getIndividualName() + "." + getPropertyName() + "( " + getValuesName() + ")";
    }
}
