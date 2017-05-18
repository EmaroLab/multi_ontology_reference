package it.emarolab.amor.owlInterface;

import com.google.common.base.Objects;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import javax.annotation.Nonnegative;

// todo connect to OWLReference

/**
 * Interface to deal with restriction in class definition as well as in data and object properties
 * domain and range definition.
 * @param <S> the type of the subject of the restriction in which it is applied to.
 * @param <A> the type of axiom describing the restriction.
 * @param <V> the type of value that the restriction is applying to the subject trhough the axiom.
 */
//@SuppressWarnings({"WeakerAccess", "unused"})
public interface SemanticRestriction<S extends OWLObject, A extends OWLAxiom, V extends OWLObject> {

    /**
     * @return the semantic entity in which the restriction is applied to.
     */
    S getSubject();
    /**
     * @param s the subject of the description to set.
     */
    void setSubject( S s);
    /**
     * @return the name of {@link #getSubject()}, given through {@link OWLReferencesInterface#getOWLName(OWLObject)}
     */
    default String getSubjectName(){
        return OWLReferencesInterface.getOWLName( getSubject());
    }

    /**
     * @return the value of the restriction applied through the axiom.
     */
    V getValue();
    /**
     * @param v the value of the restriction to set.
     */
    void setValue( V v);
    /**
     * @return the name of {@link #getValue()}, given through {@link OWLReferencesInterface#getOWLName(OWLObject)}
     */
    default String getValueName(){
        return OWLReferencesInterface.getOWLName( getValue());
    }

    /**
     * Computes the axiom that define the {@code value} restricted to the {@code subject}.
     * @param ontology the representation that will host the restriction.
     * @return the description of the restriction ready to be applied or removed.
     */
    A getAxiom( OWLReferencesInterface ontology);
    /**
     * Computes the specific restriction with the specified {@code value}.
     * @param ontology the representation that will host the restriction.
     * @return the restriction that will be used to compute the axiom by {@link #getAxiom(OWLReferencesInterface)}.
     */
    OWLClassExpression getRestriction( OWLReferencesInterface ontology);

    /**
     * Returns the changes to be done in order to add the specified restriction to an ontology.
     * It is based on ({@link #getAxiom(OWLReferencesInterface)}).
     * @param ontoManipulator the manipulator to the representation that will host the restriction.
     * @return the changes to be done in the ontology to add the restriction.
     */
    default OWLOntologyChange addRestriction( OWLManipulator ontoManipulator){
        try {
            OWLOntologyChange add = ontoManipulator.getAddAxiom(getAxiom(ontoManipulator.getOwlLibrary()));
            if( ! ontoManipulator.isChangeBuffering())
                ontoManipulator.applyChanges(add);
            return add;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoManipulator.getOwlLibrary().logInconsistency();
            return null;
        }
    }
    /**
     * Returns the changes to be done in order to remove the specified restriction from an ontology.
     * It is based on ({@link #getAxiom(OWLReferencesInterface)}).
     * @param ontoManipulator the manipulator to the representation that will not host the restriction anymore.
     * @return the changes to be done in the ontology to remove the restriction.
     */
    default OWLOntologyChange removeRestriction( OWLManipulator ontoManipulator){
        try {
            OWLOntologyChange remove = ontoManipulator.getRemoveAxiom(getAxiom(ontoManipulator.getOwlLibrary()));
            if( ! ontoManipulator.isChangeBuffering())
                ontoManipulator.applyChanges(remove);
            return remove;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoManipulator.getOwlLibrary().logInconsistency();
            return null;
        }
    }

    /**
     * It converts a java class in the related OWL data factory.
     * Supported types are: {@code Double}, {@code Float}, {@code String},
     * {@code Boolean}, {@code Long} and {@code Integer}.
     * It returns {@code null} if the input class is not supported.
     * @param c the java class to be converted in a OWL data type.
     * @return the OWL data type related to the input class,
     * {@code null} if the type is not supported.
     */
    default OWLDatatype getDataType( Class c){
        OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        if( c.equals( Double.class))
            return factory.getDoubleOWLDatatype();
        else if( c.equals( Float.class))
            return factory.getFloatOWLDatatype();
        else if( c.equals( String.class))
            return factory.getStringOWLDatatype();
        else if( c.equals( Boolean.class))
            return factory.getBooleanOWLDatatype();
        else if( c.equals( Long.class))
            return factory.getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI());
        else if( c.equals( Integer.class))
            return factory.getIntegerOWLDatatype();
        else
            return null;
    }

    /**
     * It defines the basic features that a restriction must have when it involves a data or object property.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     * @param <P> the type of property to restrict with.
     * @param <V> the type of value that the restriction is applying to the subject thoughh the axiom.
     */
    interface PropertyRestriction<S extends OWLObject,A extends OWLAxiom,P extends OWLProperty,V extends OWLObject>
                extends SemanticRestriction<S,A,V>{
        /**
         * @return the object or data property used in the restriction.
         */
        P getProperty();
        /**
         * @param p the data or object property to set.
         */
        void setProperty( P p);
        /**
         * @return the name of {@link #getProperty()}, given through {@link OWLReferencesInterface#getOWLName(OWLObject)}
         */
        default String getPropertyName(){
            return OWLReferencesInterface.getOWLName( getProperty());
        }
    }

    /**
     * It defines the basic features that a restriction must have when it involves a cardinality over a data or object property.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     * @param <P> the type of property to restrict with.
     * @param <V> the type of value that the restriction is applying to the subject thoughh the axiom.
     */
    interface PropertyCardinalityRestriction<S extends OWLObject,A extends OWLAxiom,P extends OWLProperty,V extends OWLObject>
            extends PropertyRestriction<S,A,P,V>{

        /**
         * @param c the cardinality of the data or object property description to set. It cannot be negative.
         */
        void setCardinality( @Nonnegative int c);
        /**
         * @return the cardinality of the restriction over a data or object property.
         */
        @Nonnegative int getCardinality();
    }

    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for minimum cardinality restriction over a data property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLDataMinCardinality( {@link #getCardinality()}, {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface DataMinRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyCardinalityRestriction<S,A,OWLDataProperty,OWLDataRange>{
        @Override
        default OWLDataMinCardinality getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLDataMinCardinality( getCardinality(), getProperty(), getValue());
        }
    }
    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for minimum cardinality restriction over an object property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLObjectMinCardinality( {@link #getCardinality()}, {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface ObjectMinRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyCardinalityRestriction<S,A,OWLObjectProperty,OWLClass>{
        @Override
        default OWLObjectMinCardinality getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLObjectMinCardinality( getCardinality(), getProperty(), getValue());
        }
    }

    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for maximum cardinality restriction over a data property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLDataMaxCardinality( {@link #getCardinality()}, {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface DataMaxRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyCardinalityRestriction<S,A,OWLDataProperty,OWLDataRange>{
        @Override
        default OWLDataMaxCardinality getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLDataMaxCardinality( getCardinality(), getProperty(), getValue());
        }
    }
    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for maximum cardinality restriction over an object property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLObjectMaxCardinality( {@link #getCardinality()}, {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface ObjectMaxRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyCardinalityRestriction<S,A,OWLObjectProperty,OWLClass>{
        @Override
        default OWLObjectMaxCardinality getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLObjectMaxCardinality( getCardinality(), getProperty(), getValue());
        }
    }

    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for exact cardinality restriction over a data property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLDataExactCardinality( {@link #getCardinality()}, {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface DataExactRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyCardinalityRestriction<S,A,OWLDataProperty,OWLDataRange>{
        @Override
        default OWLDataExactCardinality getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLDataExactCardinality( getCardinality(), getProperty(), getValue());
        }
    }
    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for exact cardinality restriction over an object property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLObjectExactCardinality( {@link #getCardinality()}, {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface ObjectExactRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyCardinalityRestriction<S,A,OWLObjectProperty,OWLClass>{
        @Override
        default OWLObjectExactCardinality getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLObjectExactCardinality( getCardinality(), getProperty(), getValue());
        }
    }

    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for existential restriction over a data property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLDataSomeValuesFrom( {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface DataSomeRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyRestriction<S,A,OWLDataProperty,OWLDataRange>{
        @Override
        default OWLDataSomeValuesFrom getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLDataSomeValuesFrom(getProperty(), getValue());
        }
    }
    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for existential restriction over an object property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLObjectSomeValuesFrom( {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface ObjectSomeRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyRestriction<S,A,OWLObjectProperty,OWLClass>{
        @Override
        default OWLObjectSomeValuesFrom getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLObjectSomeValuesFrom( getProperty(), getValue());
        }
    }

    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for universal restriction over a data property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLDataAllValuesFrom( {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface DataAllRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyRestriction<S,A,OWLDataProperty,OWLDataRange>{
        @Override
        default OWLDataAllValuesFrom getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLDataAllValuesFrom(getProperty(), getValue());
        }
    }
    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for universal restriction over an object property. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLObjectAllValuesFrom( {@link #getProperty()}, getValue}}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface ObjectAllRestriction<S extends OWLObject,A extends OWLAxiom>
            extends PropertyRestriction<S,A,OWLObjectProperty,OWLClass>{
        @Override
        default OWLObjectAllValuesFrom getRestriction(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLObjectAllValuesFrom( getProperty(), getValue());
        }
    }

    /**
     * It implements the {@link #getRestriction(OWLReferencesInterface)}
     * for univeral restriction over a class. It is applicable to a {@link #getSubject()},
     * through {@link #getAxiom(OWLReferencesInterface)}, to be added or removed in an ontology.
     * Indeed it just returns {@link #getValue()} as {@link #getRestriction(OWLReferencesInterface)}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    interface ClassRestriction<S extends OWLObject,A extends OWLAxiom>
            extends SemanticRestriction<S,A,OWLClass>{
        @Override
        default OWLClass getRestriction(OWLReferencesInterface ontology) {
            return getValue();
        }
    }


    /**
     * It uniquely describes the different type of restriction trhough a string given interlally on constructor.
     */
    class RestrictionType {

        // used to compose the below constants and to descrimita them on boolean is... methods
        private static final String D = "_";
        private static final String CLASS_DEF = "C-DEFINITION" + D;
        private static final String DATA_DOMAIN = "D-DOMAIN" + D;
        private static final String DATA_RANGE = "D-RANGE" + D;
        private static final String OBJECT_DOMAIN = "O-DOMAIN" + D;
        private static final String OBJECT_RANGE = "O-RANGE" + D;
        private static final String CLASS = "CLASS";
        private static final String DATA = "DATA";
        private static final String OBJECT = "OBJECT";
        private static final String MIN = "MIN" + D;
        private static final String MAX = "MAX" + D;
        private static final String EXACT = "EXACT" + D;
        private static final String SOME = "SOME" + D;
        private static final String ALL = "ALL" + D;

        // subj_restriction_prop
        /**
         * defines a restriction of a class defined as equivalent to another class.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_CLASS = CLASS_DEF + CLASS;
        /**
         * defines a restriction of a class defined through a minimum number of specified data properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_MIN_DATA = CLASS_DEF + MIN + DATA;
        /**
         * defines a restriction of a class defined through a maximum number of specified data properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_MAX_DATA = CLASS_DEF + MAX + DATA;
        /**
         * defines a restriction of a class defined through an exact number of specified data properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_EXACT_DATA = CLASS_DEF + EXACT + DATA;
        /**
         * defines an universal restriction of a class through all data properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_ALL_DATA = CLASS_DEF + ALL + DATA;
        /**
         * defines an existential restriction of a class through some data properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_SOME_DATA = CLASS_DEF + SOME + DATA;
        /**
         * defines a restriction of a class defined through a minimum number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_MIN_OBJECT = CLASS_DEF + MIN + OBJECT;
        /**
         * defines a restriction of a class defined through a maximum number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_MAX_OBJECT = CLASS_DEF + MAX + OBJECT;
        /**
         * defines a restriction of a class defined through an exact number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_EXACT_OBJECT = CLASS_DEF + EXACT + OBJECT;
        /**
         * defines an universal restriction of a class through all data properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_ALL_OBJECT = CLASS_DEF + ALL + OBJECT;
        /**
         * defines an existential restriction of a class through some object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DEF_CLASS_SOME_OBJECT = CLASS_DEF + SOME + OBJECT;

        /**
         * defines a restriction of a data property domain to be in another class.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_CLASS = DATA_DOMAIN + CLASS;
        /**
         * defines a restriction of a data property domain to respect a minimum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_MIN_DATA = DATA_DOMAIN + MIN + DATA;
        /**
         * defines a restriction of a data property domain to respect a maximum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_MAX_DATA = DATA_DOMAIN  + MAX + DATA;
        /**
         * defines a restriction of a data property domain to respect an exact number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_EXACT_DATA = DATA_DOMAIN  + EXACT + DATA;
        /**
         * defines an universal restriction of a data property domain to respect with respect to a specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_ALL_DATA = DATA_DOMAIN + ALL + DATA;
        /**
         * defines an existential restriction of a data property domain to respect with respect to a specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_SOME_DATA = DATA_DOMAIN + SOME + DATA;
        /**
         * defines a restriction of a data property domain to respect a minimum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_MIN_OBJECT = DATA_DOMAIN + MIN + OBJECT;
        /**
         * defines a restriction of a data property domain to respect a maximum number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_MAX_OBJECT = DATA_DOMAIN  + MAX + OBJECT;
        /**
         * defines a restriction of a data property domain to respect an exact number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_EXACT_OBJECT = DATA_DOMAIN  + EXACT + OBJECT;
        /**
         * defines an universal restriction of a data property domain to respect with respect to a specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_ALL_OBJECT = DATA_DOMAIN + ALL + OBJECT;
        /**
         * defines an existential restriction of a data property domain to respect with respect to a specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_DOMAIN_SOME_OBJECT = DATA_DOMAIN + SOME + OBJECT;

        /**
         * defines an universal restriction of a data property domain to respect with respect to a specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String DATA_RANGE_ALL_DATA = DATA_RANGE + ALL + DATA;

        /**
         * defines a restriction of an object property domain to be in another class.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_CLASS = OBJECT_DOMAIN + CLASS;
        /**
         * defines a restriction of an object property domain to respect a minimum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_MIN_DATA = OBJECT_DOMAIN + MIN + DATA;
        /**
         * defines a restriction of an object property domain to respect a maximum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_MAX_DATA = OBJECT_DOMAIN  + MAX + DATA;
        /**
         * defines a restriction of an object property domain to respect an exact number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_EXACT_DATA = OBJECT_DOMAIN  + EXACT + DATA;
        /**
         * defines an universal restriction of an object property domain to respect with respect to a specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_ALL_DATA = OBJECT_DOMAIN + ALL + DATA;
        /**
         * defines an existential restriction of an object property domain to respect with respect to a specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_SOME_DATA = OBJECT_DOMAIN + SOME + DATA;
        /**
         * defines a restriction of an object property domain to respect a minimum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_MIN_OBJECT = OBJECT_DOMAIN + MIN + OBJECT;
        /**
         * defines a restriction of an object property domain to respect an exact number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_MAX_OBJECT = OBJECT_DOMAIN  + MAX + OBJECT;
        /**
         * defines a restriction of an object property domain to respect an exact number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_EXACT_OBJECT = OBJECT_DOMAIN  + EXACT + OBJECT;
        /**
         * defines an universal restriction of an object property domain to respect with respect to a specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_ALL_OBJECT = OBJECT_DOMAIN + ALL + OBJECT;
        /**
         * defines an existential restriction of an object property domain to respect with respect to a specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_DOMAIN_SOME_OBJECT = OBJECT_DOMAIN + SOME + OBJECT;

        /**
         * defines a restriction of an object property range to be in another class.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_CLASS = OBJECT_RANGE + CLASS;
        /**
         * defines a restriction of an object property range to respect a minimum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_MIN_DATA = OBJECT_RANGE + MIN + DATA;
        /**
         * defines a restriction of an object property range ti respect a maximum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_MAX_DATA = OBJECT_RANGE  + MAX + DATA;
        /**
         * defines a restriction of an object property range ti respect an exact number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_EXACT_DATA = OBJECT_RANGE  + EXACT + DATA;
        /**
         * defines an universal restriction of an object property range ti respect with respect to a specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_ALL_DATA = OBJECT_RANGE + ALL + DATA;
        /**
         * defines an existential restriction of an object property range ti respect with respect to a specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_SOME_DATA = OBJECT_RANGE + SOME + DATA;
        /**
         * defines a restriction of an object property range ti respect a minimum number of specified data type.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_MIN_OBJECT = OBJECT_RANGE + MIN + OBJECT;
        /**
         * defines a restriction of an object property range ti respect an exact number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_MAX_OBJECT = OBJECT_RANGE  + MAX + OBJECT;
        /**
         * defines a restriction of an object property range ti respect an exact number of specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_EXACT_OBJECT = OBJECT_RANGE  + EXACT + OBJECT;
        /**
         * defines an universal restriction of an object property range ti respect with respect to a specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_ALL_OBJECT = OBJECT_RANGE + ALL + OBJECT;
        /**
         * defines an existential restriction of an object property range ti respect with respect to a specified object properties.
         * This can be in conjunction with other restrictions.
         */
        public static final String OBJECT_RANGE_SOME_OBJECT = OBJECT_RANGE + SOME + OBJECT;

        private String type;

        /**
         * @param type the type of the restriction. It is one of th constants defined in this class.
         */
        protected RestrictionType(String type) {
            this.type = type;
        }

        /**
         * @return the type of this restriction. It is one of th constants defined in this class.
         */
        public String getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SemanticRestriction.RestrictionType)) return false;
            RestrictionType that = (RestrictionType) o;
            return Objects.equal(getType(), that.getType());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getType());
        }

        /**
         * @return {@code true} if the restriction is applied as class definition.
         */
        public boolean isClassDefinition(){
            return getType().contains(CLASS_DEF);
        }
        /**
         * @return {@code true} if the restriction is applied as data property domain.
         */
        public boolean isDataPropertyDomain(){
            return getType().contains( DATA_DOMAIN);
        }
        /**
         * @return {@code true} if the restriction is applied as data property range.
         */
        public boolean isDataPropertyRange(){
            return getType().contains( DATA_RANGE);
        }
        /**
         * @return {@code true} if the restriction is applied as object property domain.
         */
        public boolean isObjectPropertyDomain(){
            return getType().contains( OBJECT_DOMAIN);
        }
        /**
         * @return {@code true} if the restriction is applied as object property range.
         */
        public boolean isObjectPropertyRange(){
            return getType().contains( OBJECT_RANGE);
        }

        /**
         * @return {@code true} if it restricts over a class.
         */
        public boolean isRestrictionOnClass(){
            return getType().contains( CLASS);
        }
        /**
         * @return {@code true} if it restricts over a data property (data type).
         */
        public boolean isRestrictionOnDataProperty(){
            return getType().contains( DATA);
        }
        /**
         * @return {@code true} if it restricts over an object property (class).
         */
        public boolean isRestrictionOnObjectProperty(){
            return getType().contains( OBJECT);
        }

        /**
         * @return {@code true} if it restricts as minimal cardinality.
         */
        public boolean isMinRestriction(){
            return getType().contains( MIN);
        }
        /**
         * @return {@code true} if it restricts as maximal cardinality.
         */
        public boolean isMaxRestriction(){
            return getType().contains( MAX);
        }
        /**
         * @return {@code true} if it restricts as exact cardinality.
         */
        public boolean isExactRestriction(){
            return getType().contains( EXACT);
        }
        /**
         * @return {@code true} if it restricts as esistential cardinality.
         */
        public boolean isSomeRestriction(){
            return getType().contains( SOME);
        }
        /**
         * @return {@code true} if it restricts as universal cardinality.
         */
        public boolean isAllRestriction(){
            return getType().contains( ALL);
        }

        @Override
        public String toString() {
            return type;
        }
    }


    /**
     * It is an implementation of {@link SemanticRestriction}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     * @param <V> the type of value that the restriction is applying to the subject trhough the axiom.
     */
    abstract class ApplyingRestriction<S extends OWLObject,A extends OWLAxiom,V  extends OWLObject>
            implements SemanticRestriction<S,A,V>{

        private S subject;
        private V value;
        /**
         * This uniqualy describes the restriction type through one of the constants defined
         * in {@link RestrictionType}. It is given on constructor through {@link #setRestrictionType()} and should not be changed later.
         */
        protected RestrictionType restrictionType;

        /**
         * Construct this object by only setting the {@link #restrictionType}. All other fields are null.
         */
        public ApplyingRestriction(){
            setRestrictionType();
        }
        /**
         * Construct this object by only setting the {@link #restrictionType} and the subject of the restriction.
         * All other fields are null.
         * @param subject the semantic entity in whic the restriction is applied.
         */
        public ApplyingRestriction(S subject){
            setRestrictionType();
            setSubject( subject);
        }
        /**
         * Fully construct this object.
         * @param subject the semantic entity in whic the restriction is applied.
         * @param value the value of the restriction.
         */
        public ApplyingRestriction(S subject, V value){
            setRestrictionType();
            setSubject( subject);
            setValue( value);
        }

        @Override
        public S getSubject() {
            return this.subject;
        }
        @Override
        public void setSubject(S s) {
            this.subject = s;
        }

        @Override
        public V getValue() {
            return value;
        }
        @Override
        public void setValue(V v) {
            this.value = v;
        }

        /**
         * @return an unique descriptor of the type of the restriction applied from the {@code subject} to the {@code value}
         */
        public final RestrictionType getRestrictionType() {
            return restrictionType;
        }
        /**
         * This method is called during all constrcutors and should set the restriction type as constant and unique.
         */
        abstract protected void setRestrictionType();

        @Override
        public boolean equals(Object o) { // if all fields are equals
            if (this == o) return true;
            if (!(o instanceof ApplyingRestriction)) return false;
            ApplyingRestriction<?, ?, ?> that = (ApplyingRestriction<?, ?, ?>) o;
            return Objects.equal(getSubject(), that.getSubject()) &&
                    Objects.equal(getValue(), that.getValue()) &&
                    Objects.equal(getRestrictionType(), that.getRestrictionType());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getSubject(), getValue(), getRestrictionType());
        }

        @Override
        public String toString() {
            return restrictionType + " : " + getSubjectName() + " -> (" + getValueName() + ")";
        }
    }

    /**
     * This is an implementation of {@link PropertyRestriction}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     * @param <P> the type of property to restrict with.
     * @param <V> the type of value that the restriction is applying to the subject thoughh the axiom.
     */
    abstract class ApplyingPropertyRestriction<S extends OWLObject,A extends OWLAxiom,P extends OWLProperty,V  extends OWLObject>
            extends ApplyingRestriction<S,A,V>
            implements PropertyRestriction<S,A,P,V>{

        private P property;

        /**
         * it is based on super constructor {@link ApplyingRestriction} and its purposes is only
         * to set the field with the specified value, as well as the {@link #restrictionType}.
         */
        public ApplyingPropertyRestriction() {
            super();
        }
        /**
         * it is based on super constructor {@link ApplyingRestriction} and its purposes is only
         * to set the field with the specified value, as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         */
        public ApplyingPropertyRestriction(S subject) {
            super(subject);
        }
        /**
         * it is based on super constructor {@link ApplyingRestriction} and its purposes is only
         * to set the field with the specified value, as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param value the value of the restriction.
         */
        public ApplyingPropertyRestriction(S subject, V value) {
            super(subject, value);
        }
        /**
         * it is based on super constructor {@link ApplyingRestriction} and its purposes is only
         * to set the field with the specified value, as well as the {@link #restrictionType}.
         * @param property the data or object property of the description.
         */
        public ApplyingPropertyRestriction(P property) {
            super();
            setProperty( property);
        }
        /**
         * it is based on super constructor {@link ApplyingRestriction} and its purposes is only
         * to set the field with the specified value, as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param property the data or object property of the description.
         */
        public ApplyingPropertyRestriction(S subject, P property) {
            super(subject);
            setProperty( property);
        }
        /**
         * it is based on super constructor {@link ApplyingRestriction} and its purposes is only
         * to set the field with the specified value, as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param value the value of the restriction.
         * @param property the data or object property of the description.
         */
        public ApplyingPropertyRestriction(S subject, V value, P property) {
            super(subject, value);
            setProperty( property);
        }

        @Override
        public P getProperty() {
            return property;
        }
        @Override
        public void setProperty(P p) {
            this.property = p;
        }

        @Override
        public boolean equals(Object o) { // if all fields are equals
            if (this == o) return true;
            if (!(o instanceof ApplyingPropertyRestriction)) return false;
            if (!super.equals(o)) return false;
            ApplyingPropertyRestriction<?, ?, ?, ?> that = (ApplyingPropertyRestriction<?, ?, ?, ?>) o;
            return Objects.equal(getProperty(), that.getProperty());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), getProperty());
        }

        @Override
        public String toString() {
            return restrictionType + " : " + getSubjectName() + " -> " + getPropertyName() + "(" + getValueName() + ")";
        }
    }

    /**
     * This is an implementation of {@link PropertyCardinalityRestriction}.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     * @param <P> the type of property to restrict with.
     * @param <V> the type of value that the restriction is applying to the subject thoughh the axiom.
     */
    abstract class ApplyingCardinalityRestriction<S extends OWLObject,A extends OWLAxiom,P extends OWLProperty,V  extends OWLObject>
            extends ApplyingPropertyRestriction<S,A,P,V>
            implements PropertyCardinalityRestriction<S,A,P,V>{

        private int cardinality; // no negative

        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         */
        public ApplyingCardinalityRestriction() {
            super();
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         */
        public ApplyingCardinalityRestriction(S subject) {
            super(subject);
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param value the value of the restriction.
         */
        public ApplyingCardinalityRestriction(S subject, V value) {
            super(subject, value);
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param property the data or object property of the description.
         */
        public ApplyingCardinalityRestriction(S subject, P property) {
            super(subject, property);
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param value the value of the restriction.
         * @param property the data or object property of the description.
         */
        public ApplyingCardinalityRestriction(S subject, V value, P property) {
            super(subject, value, property);
        }

        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param cardinality the cardinality to be applied to the number of properties for the restriction.
         */
        public ApplyingCardinalityRestriction(int cardinality) {
            this.cardinality = cardinality;
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param value the value of the restriction.
         * @param cardinality the cardinality to be applied to the number of properties for the restriction.
         */
        public ApplyingCardinalityRestriction(S subject, V value, int cardinality) {
            super(subject, value);
            this.cardinality = cardinality;
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param property the data or object property of the description.
         * @param cardinality the cardinality to be applied to the number of properties for the restriction.
         */
        public ApplyingCardinalityRestriction(P property, int cardinality) {
            super(property);
            this.cardinality = cardinality;
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param property the data or object property of the description.
         * @param cardinality the cardinality to be applied to the number of properties for the restriction.
         */
        public ApplyingCardinalityRestriction(S subject, P property, int cardinality) {
            super(subject, property);
            this.cardinality = cardinality;
        }
        /**
         * it is based on super constructor {@link ApplyingPropertyRestriction} and its purposes is only
         * to set the field with the specified value as well as the {@link #restrictionType}.
         * @param subject the subject in which the restriction in subject to.
         * @param value the value of the restriction.
         * @param property the data or object property of the description.
         * @param cardinality the cardinality to be applied to the number of properties for the restriction.
         */
        public ApplyingCardinalityRestriction(S subject, V value, P property, int cardinality) {
            super(subject, value, property);
            this.cardinality = cardinality;
        }

        @Override
        public int getCardinality() {
            return cardinality;
        }
        @Override
        public void setCardinality(@Nonnegative int c) {
            cardinality = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ApplyingCardinalityRestriction)) return false;
            if (!super.equals(o)) return false;
            ApplyingCardinalityRestriction<?, ?, ?, ?> that = (ApplyingCardinalityRestriction<?, ?, ?, ?>) o;
            return getCardinality() == that.getCardinality();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), getCardinality());
        }

        @Override
        public String toString() {
            return restrictionType + " : " + getSubjectName() + " -> " + getPropertyName() + " " + getCardinality() + "(" + getValueName() + ")";
        }
    }


    /**
     * This class combines {@link ApplyingRestriction} with the {@link ClassRestriction}
     * interface in order to define a restriction defined to be equivalent to a class.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingClassRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingRestriction<S,A,OWLClass>
            implements ClassRestriction<S,A>{
        public ApplyingClassRestriction() {
            super();
        }
        public ApplyingClassRestriction(S subject) {
            super(subject);
        }
        public ApplyingClassRestriction(S subject, OWLClass value) {
            super(subject, value);
        }
    }
    
    /**
     * This class combines {@link ApplyingCardinalityRestriction} with the {@link DataMinRestriction}
     * interface in order to define a minimum cardinality restriction though a data property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingDataMinRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingCardinalityRestriction<S,A,OWLDataProperty,OWLDataRange>
            implements DataMinRestriction<S,A>{
        public ApplyingDataMinRestriction() {
            super();
        }
        public ApplyingDataMinRestriction(S subject) {
            super(subject);
        }
        public ApplyingDataMinRestriction(S subject, OWLDataRange value) {
            super(subject, value);
        }
        public ApplyingDataMinRestriction(S subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ApplyingDataMinRestriction(S subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ApplyingDataMinRestriction(int cardinality) {
            super(cardinality);
        }
        public ApplyingDataMinRestriction(S subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ApplyingDataMinRestriction(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ApplyingDataMinRestriction(S subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ApplyingDataMinRestriction(S subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ApplyingDataMinRestriction(OWLDataMinCardinality restriction) {
            super();
            setProperty( restriction.getProperty().asOWLDataProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller());
        }
        public ApplyingDataMinRestriction(S subject, OWLDataMinCardinality restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLDataProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller());
        }
    }
    /**
     * This class combines {@link ApplyingCardinalityRestriction} with the {@link DataMaxRestriction}
     * interface in order to define a maximum cardinality restriction though a data property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingDataMaxRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingCardinalityRestriction<S,A,OWLDataProperty,OWLDataRange>
            implements DataMaxRestriction<S,A>{
        public ApplyingDataMaxRestriction() {
            super();
        }
        public ApplyingDataMaxRestriction(S subject) {
            super(subject);
        }
        public ApplyingDataMaxRestriction(S subject, OWLDataRange value) {
            super(subject, value);
        }
        public ApplyingDataMaxRestriction(S subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ApplyingDataMaxRestriction(S subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ApplyingDataMaxRestriction(int cardinality) {
            super(cardinality);
        }
        public ApplyingDataMaxRestriction(S subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ApplyingDataMaxRestriction(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ApplyingDataMaxRestriction(S subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ApplyingDataMaxRestriction(S subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ApplyingDataMaxRestriction(OWLDataMaxCardinality restriction) {
            super();
            setProperty( restriction.getProperty().asOWLDataProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller());
        }
        public ApplyingDataMaxRestriction(S subject, OWLDataMaxCardinality restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLDataProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller());
        }
    }
    /**
     * This class combines {@link ApplyingCardinalityRestriction} with the {@link DataExactRestriction}
     * interface in order to define a exact cardinality restriction though a data property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingDataExactRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingCardinalityRestriction<S,A,OWLDataProperty,OWLDataRange>
            implements DataExactRestriction<S,A>{
        public ApplyingDataExactRestriction() {
            super();
        }
        public ApplyingDataExactRestriction(S subject) {
            super(subject);
        }
        public ApplyingDataExactRestriction(S subject, OWLDataRange value) {
            super(subject, value);
        }
        public ApplyingDataExactRestriction(S subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ApplyingDataExactRestriction(S subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ApplyingDataExactRestriction(int cardinality) {
            super(cardinality);
        }
        public ApplyingDataExactRestriction(S subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ApplyingDataExactRestriction(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ApplyingDataExactRestriction(S subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ApplyingDataExactRestriction(S subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ApplyingDataExactRestriction(OWLDataExactCardinality restriction) {
            super();
            setProperty( restriction.getProperty().asOWLDataProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller());
        }
        public ApplyingDataExactRestriction(S subject, OWLDataExactCardinality restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLDataProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller());
        }
    }
    /**
     * This class combines {@link ApplyingPropertyRestriction} with the {@link DataSomeRestriction}
     * interface in order to define an existential cardinality restriction though a data property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingDataSomeRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingPropertyRestriction<S,A,OWLDataProperty,OWLDataRange>
            implements DataSomeRestriction<S,A>{
        public ApplyingDataSomeRestriction() {
            super();
        }
        public ApplyingDataSomeRestriction(S subject) {
            super(subject);
        }
        public ApplyingDataSomeRestriction(S subject, OWLDataRange value) {
            super(subject, value);
        }
        public ApplyingDataSomeRestriction(S subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ApplyingDataSomeRestriction(S subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ApplyingDataSomeRestriction(OWLDataSomeValuesFrom restriction) {
            super();
            setProperty( restriction.getProperty().asOWLDataProperty());
            setValue( restriction.getFiller());
        }
        public ApplyingDataSomeRestriction(S subject, OWLDataSomeValuesFrom restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLDataProperty());
            setValue( restriction.getFiller());
        }
    }
    /**
     * This class combines {@link ApplyingPropertyRestriction} with the {@link DataAllRestriction}
     * interface in order to define an universal cardinality restriction though a data property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingDataAllRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingPropertyRestriction<S,A,OWLDataProperty,OWLDataRange>
            implements DataAllRestriction<S,A> {
        public ApplyingDataAllRestriction() {
            super();
        }
        public ApplyingDataAllRestriction(S subject) {
            super(subject);
        }
        public ApplyingDataAllRestriction(S subject, OWLDataRange value) {
            super(subject, value);
        }
        public ApplyingDataAllRestriction(S subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ApplyingDataAllRestriction(S subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ApplyingDataAllRestriction(OWLDataAllValuesFrom restriction) {
            super();
            setProperty( restriction.getProperty().asOWLDataProperty());
            setValue( restriction.getFiller());
        }
        public ApplyingDataAllRestriction(S subject, OWLDataAllValuesFrom restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLDataProperty());
            setValue( restriction.getFiller());
        }
    }

    /**
     * This class combines {@link ApplyingCardinalityRestriction} with the {@link ObjectMinRestriction}
     * interface in order to define a minimum cardinality restriction though an object property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingObjectMinRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingCardinalityRestriction<S,A,OWLObjectProperty,OWLClass>
            implements ObjectMinRestriction<S,A>{
        public ApplyingObjectMinRestriction() {
            super();
        }
        public ApplyingObjectMinRestriction(S subject) {
            super(subject);
        }
        public ApplyingObjectMinRestriction(S subject, OWLClass value) {
            super(subject, value);
        }
        public ApplyingObjectMinRestriction(S subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ApplyingObjectMinRestriction(S subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ApplyingObjectMinRestriction(int cardinality) {
            super(cardinality);
        }
        public ApplyingObjectMinRestriction(S subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ApplyingObjectMinRestriction(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ApplyingObjectMinRestriction(S subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ApplyingObjectMinRestriction(S subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ApplyingObjectMinRestriction(OWLObjectMinCardinality restriction) {
            super();
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller().asOWLClass());
        }
        public ApplyingObjectMinRestriction(S subject, OWLObjectMinCardinality restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller().asOWLClass());
        }
    }
    /**
     * This class combines {@link ApplyingCardinalityRestriction} with the {@link ObjectMinRestriction}
     * interface in order to define a minimum cardinality restriction though an object property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingObjectMaxRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingCardinalityRestriction<S,A,OWLObjectProperty,OWLClass>
            implements ObjectMaxRestriction<S,A>{
        public ApplyingObjectMaxRestriction() {
            super();
        }
        public ApplyingObjectMaxRestriction(S subject) {
            super(subject);
        }
        public ApplyingObjectMaxRestriction(S subject, OWLClass value) {
            super(subject, value);
        }
        public ApplyingObjectMaxRestriction(S subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ApplyingObjectMaxRestriction(S subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ApplyingObjectMaxRestriction(int cardinality) {
            super(cardinality);
        }
        public ApplyingObjectMaxRestriction(S subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ApplyingObjectMaxRestriction(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ApplyingObjectMaxRestriction(S subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ApplyingObjectMaxRestriction(S subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ApplyingObjectMaxRestriction(OWLObjectMaxCardinality restriction) {
            super();
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller().asOWLClass());
        }
        public ApplyingObjectMaxRestriction(S subject, OWLObjectMaxCardinality restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller().asOWLClass());
        }
    }
    /**
     * This class combines {@link ApplyingCardinalityRestriction} with the {@link ObjectExactRestriction}
     * interface in order to define an exact cardinality restriction though an object property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingObjectExactRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingCardinalityRestriction<S,A,OWLObjectProperty,OWLClass>
            implements ObjectExactRestriction<S,A>{
        public ApplyingObjectExactRestriction() {
            super();
        }
        public ApplyingObjectExactRestriction(S subject) {
            super(subject);
        }
        public ApplyingObjectExactRestriction(S subject, OWLClass value) {
            super(subject, value);
        }
        public ApplyingObjectExactRestriction(S subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ApplyingObjectExactRestriction(S subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ApplyingObjectExactRestriction(int cardinality) {
            super(cardinality);
        }
        public ApplyingObjectExactRestriction(S subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ApplyingObjectExactRestriction(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ApplyingObjectExactRestriction(S subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ApplyingObjectExactRestriction(S subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ApplyingObjectExactRestriction(OWLObjectExactCardinality restriction) {
            super();
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller().asOWLClass());
        }
        public ApplyingObjectExactRestriction(S subject, OWLObjectExactCardinality restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setCardinality( restriction.getCardinality());
            setValue( restriction.getFiller().asOWLClass());
        }
    }
    /**
     * This class combines {@link ApplyingPropertyRestriction} with the {@link ObjectSomeRestriction}
     * interface in order to define an existential restriction though an object property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingObjectSomeRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingPropertyRestriction<S,A,OWLObjectProperty,OWLClass>
            implements ObjectSomeRestriction<S,A>{
        public ApplyingObjectSomeRestriction() {
            super();
        }
        public ApplyingObjectSomeRestriction(S subject) {
            super(subject);
        }
        public ApplyingObjectSomeRestriction(S subject, OWLClass value) {
            super(subject, value);
        }
        public ApplyingObjectSomeRestriction(S subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ApplyingObjectSomeRestriction(S subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ApplyingObjectSomeRestriction(OWLObjectSomeValuesFrom restriction) {
            super();
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setValue( restriction.getFiller().asOWLClass());
        }
        public ApplyingObjectSomeRestriction(S subject, OWLObjectSomeValuesFrom restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setValue( restriction.getFiller().asOWLClass());
        }

    }
    /**
     * This class combines {@link ApplyingPropertyRestriction} with the {@link ObjectAllRestriction}
     * interface in order to define an universal restriction though an object property.
     * It does not implement any further features, see those classes for more details.
     * @param <S> the type of the subject of the restriction in which it is applied to.
     * @param <A> the type of axiom describing the restriction.
     */
    abstract class ApplyingObjectAllRestriction<S extends OWLObject,A extends OWLAxiom>
            extends ApplyingPropertyRestriction<S,A,OWLObjectProperty,OWLClass>
            implements ObjectAllRestriction<S,A> {
        public ApplyingObjectAllRestriction() {
            super();
        }
        public ApplyingObjectAllRestriction(S subject) {
            super(subject);
        }
        public ApplyingObjectAllRestriction(S subject, OWLClass value) {
            super(subject, value);
        }
        public ApplyingObjectAllRestriction(S subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ApplyingObjectAllRestriction(S subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ApplyingObjectAllRestriction(OWLObjectAllValuesFrom restriction) {
            super();
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setValue( restriction.getFiller().asOWLClass());
        }
        public ApplyingObjectAllRestriction(S subject, OWLObjectAllValuesFrom restriction) {
            super(subject);
            setProperty( restriction.getProperty().asOWLObjectProperty());
            setValue( restriction.getFiller().asOWLClass());
        }
    }


    /**
     * This interface implements the {@link #getAxiom(OWLReferencesInterface)}
     * for restrictions that involve an {@link OWLClass} as {@code subject}.
     * It is based on {@link OWLDataFactory#getOWLSubClassOfAxiom(OWLClassExpression, OWLClassExpression)}.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLSubClassOfAxiom( {@link #getSubject()}, {@link #getRestriction(OWLReferencesInterface)})},
     * in order to link the restriction class definition to the specified subject in the ontology.
     * @param <V> the type of the value of the restriction.
     */
    interface RestrictOnClass<V extends OWLObject>
            extends SemanticRestriction<OWLClass,OWLSubClassOfAxiom,V>{
        @Override
        default OWLSubClassOfAxiom getAxiom(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLSubClassOfAxiom(getSubject(), getRestriction( ontology));
        }
    }

    /**
     * This class combines {@link ApplyingClassRestriction} with the {@link RestrictOnClass}
     * interface in order to define equivalence restriction between two classes.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_CLASS}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnClass
            extends ApplyingClassRestriction<OWLClass,OWLSubClassOfAxiom> 
            implements RestrictOnClass<OWLClass> {
        public ClassRestrictedOnClass() {
            super();
        }
        public ClassRestrictedOnClass(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnClass(OWLClass subject, OWLClass value) {
            super(subject, value);
        }


        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_CLASS);
        }
    }
    
    /**
     * This class combines {@link ApplyingDataMinRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within a minimum number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_MIN_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnMinData
            extends ApplyingDataMinRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLDataRange> {
        public ClassRestrictedOnMinData() {
            super();
        }
        public ClassRestrictedOnMinData(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnMinData(OWLClass subject, OWLDataRange value) {
            super(subject, value);
        }
        public ClassRestrictedOnMinData(OWLClass subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnMinData(OWLClass subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnMinData(int cardinality) {
            super(cardinality);
        }
        public ClassRestrictedOnMinData(OWLClass subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ClassRestrictedOnMinData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ClassRestrictedOnMinData(OWLClass subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ClassRestrictedOnMinData(OWLClass subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ClassRestrictedOnMinData(OWLDataMinCardinality restriction) {
            super(restriction);
        }
        public ClassRestrictedOnMinData(OWLClass subject, OWLDataMinCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_MIN_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataMaxRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restricion of a class within a maximum number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_MAX_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnMaxData
            extends ApplyingDataMaxRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLDataRange> {
        public ClassRestrictedOnMaxData() {
            super();
        }
        public ClassRestrictedOnMaxData(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnMaxData(OWLClass subject, OWLDataRange value) {
            super(subject, value);
        }
        public ClassRestrictedOnMaxData(OWLClass subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnMaxData(OWLClass subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnMaxData(int cardinality) {
            super(cardinality);
        }
        public ClassRestrictedOnMaxData(OWLClass subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ClassRestrictedOnMaxData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ClassRestrictedOnMaxData(OWLClass subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ClassRestrictedOnMaxData(OWLClass subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ClassRestrictedOnMaxData(OWLDataMaxCardinality restriction) {
            super(restriction);
        }
        public ClassRestrictedOnMaxData(OWLClass subject, OWLDataMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_MAX_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataExactRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within an exact number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_MAX_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnExactData
            extends ApplyingDataExactRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLDataRange> {
        public ClassRestrictedOnExactData() {
            super();
        }
        public ClassRestrictedOnExactData(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnExactData(OWLClass subject, OWLDataRange value) {
            super(subject, value);
        }
        public ClassRestrictedOnExactData(OWLClass subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnExactData(OWLClass subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnExactData(int cardinality) {
            super(cardinality);
        }
        public ClassRestrictedOnExactData(OWLClass subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ClassRestrictedOnExactData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ClassRestrictedOnExactData(OWLClass subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ClassRestrictedOnExactData(OWLClass subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ClassRestrictedOnExactData(OWLDataExactCardinality restriction) {
            super(restriction);
        }
        public ClassRestrictedOnExactData(OWLClass subject, OWLDataExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_EXACT_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataSomeRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within an existential cardinality of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_SOME_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnSomeData
            extends ApplyingDataSomeRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLDataRange>{
        public ClassRestrictedOnSomeData() {
            super();
        }
        public ClassRestrictedOnSomeData(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnSomeData(OWLClass subject, OWLDataRange value) {
            super(subject, value);
        }
        public ClassRestrictedOnSomeData(OWLClass subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnSomeData(OWLClass subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnSomeData(OWLDataSomeValuesFrom restriction) {
            super(restriction);
        }
        public ClassRestrictedOnSomeData(OWLClass subject, OWLDataSomeValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_SOME_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataAllRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within an universal cardinality of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_ALL_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnAllData
            extends ApplyingDataAllRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLDataRange> {
        public ClassRestrictedOnAllData() {
            super();
        }
        public ClassRestrictedOnAllData(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnAllData(OWLClass subject, OWLDataRange value) {
            super(subject, value);
        }
        public ClassRestrictedOnAllData(OWLClass subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnAllData(OWLClass subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnAllData(OWLDataAllValuesFrom restriction) {
            super(restriction);
        }
        public ClassRestrictedOnAllData(OWLClass subject, OWLDataAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_ALL_DATA);
        }
    }

    /**
     * This class combines {@link ApplyingObjectMinRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within a minimum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_MIN_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnMinObject
            extends ApplyingObjectMinRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLClass> {
        public ClassRestrictedOnMinObject() {
            super();
        }
        public ClassRestrictedOnMinObject(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnMinObject(OWLClass subject, OWLClass value) {
            super(subject, value);
        }
        public ClassRestrictedOnMinObject(OWLClass subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnMinObject(OWLClass subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnMinObject(int cardinality) {
            super(cardinality);
        }
        public ClassRestrictedOnMinObject(OWLClass subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ClassRestrictedOnMinObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ClassRestrictedOnMinObject(OWLClass subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ClassRestrictedOnMinObject(OWLClass subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ClassRestrictedOnMinObject(OWLObjectMinCardinality restriction) {
            super(restriction);
        }
        public ClassRestrictedOnMinObject(OWLClass subject, OWLObjectMinCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_MIN_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectMaxRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within a maximum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_MAX_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnMaxObject
            extends ApplyingObjectMaxRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLClass> {
        public ClassRestrictedOnMaxObject() {
            super();
        }
        public ClassRestrictedOnMaxObject(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnMaxObject(OWLClass subject, OWLClass value) {
            super(subject, value);
        }
        public ClassRestrictedOnMaxObject(OWLClass subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnMaxObject(OWLClass subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnMaxObject(int cardinality) {
            super(cardinality);
        }
        public ClassRestrictedOnMaxObject(OWLClass subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ClassRestrictedOnMaxObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ClassRestrictedOnMaxObject(OWLClass subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ClassRestrictedOnMaxObject(OWLClass subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ClassRestrictedOnMaxObject(OWLObjectMaxCardinality restriction) {
            super(restriction);
        }
        public ClassRestrictedOnMaxObject(OWLClass subject, OWLObjectMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_MAX_OBJECT);
        }
    }
    /**
     * This class combines {@link ClassRestrictedOnExactObject} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within a exact number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_EXACT_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnExactObject
            extends ApplyingObjectExactRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLClass> {
        public ClassRestrictedOnExactObject() {
            super();
        }
        public ClassRestrictedOnExactObject(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnExactObject(OWLClass subject, OWLClass value) {
            super(subject, value);
        }
        public ClassRestrictedOnExactObject(OWLClass subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnExactObject(OWLClass subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnExactObject(int cardinality) {
            super(cardinality);
        }
        public ClassRestrictedOnExactObject(OWLClass subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ClassRestrictedOnExactObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ClassRestrictedOnExactObject(OWLClass subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ClassRestrictedOnExactObject(OWLClass subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ClassRestrictedOnExactObject(OWLObjectExactCardinality restriction) {
            super(restriction);
        }
        public ClassRestrictedOnExactObject(OWLClass subject, OWLObjectExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_EXACT_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectSomeRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within an existential cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_SOME_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnSomeObject
            extends ApplyingObjectSomeRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLClass> {
        public ClassRestrictedOnSomeObject() {
            super();
        }
        public ClassRestrictedOnSomeObject(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnSomeObject(OWLClass subject, OWLClass value) {
            super(subject, value);
        }
        public ClassRestrictedOnSomeObject(OWLClass subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnSomeObject(OWLClass subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnSomeObject(OWLObjectSomeValuesFrom restriction) {
            super(restriction);
        }
        public ClassRestrictedOnSomeObject(OWLClass subject, OWLObjectSomeValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_SOME_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectAllRestriction} with the {@link RestrictOnClass}
     * interface in order to define a restriction of a class within an universal cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DEF_CLASS_ALL_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ClassRestrictedOnAllObject
            extends ApplyingObjectAllRestriction<OWLClass,OWLSubClassOfAxiom>
            implements RestrictOnClass<OWLClass>{
        public ClassRestrictedOnAllObject() {
            super();
        }
        public ClassRestrictedOnAllObject(OWLClass subject) {
            super(subject);
        }
        public ClassRestrictedOnAllObject(OWLClass subject, OWLClass value) {
            super(subject, value);
        }
        public ClassRestrictedOnAllObject(OWLClass subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ClassRestrictedOnAllObject(OWLClass subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ClassRestrictedOnAllObject(OWLObjectAllValuesFrom restriction) {
            super(restriction);
        }
        public ClassRestrictedOnAllObject(OWLClass subject, OWLObjectAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DEF_CLASS_ALL_OBJECT);
        }
    }


    /**
     * This interface implements the {@link #getAxiom(OWLReferencesInterface)}
     * for domain restrictions that involve an {@link OWLDataProperty} as {@code subject}.
     * It is based on {@link OWLDataFactory#getOWLDataPropertyDomainAxiom(OWLDataPropertyExpression, OWLClassExpression)}.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLDataPropertyDomainAxiom( {@link #getSubject()}, {@link #getRestriction(OWLReferencesInterface)})},
     * in order to link the restriction domain to the specified subject in the ontology.
     * @param <V> the type of the value of the restriction.
     */
    interface RestrictOnDataPropertyDomain<V extends OWLObject>
            extends SemanticRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom,V>{
        @Override
        default OWLDataPropertyDomainAxiom getAxiom(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLDataPropertyDomainAxiom(getSubject(), getRestriction(ontology));
        }
    }

    /**
     * This class combines {@link ApplyingClassRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within a class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_CLASS}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnClass
            extends ApplyingClassRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLClass> {
        public DataDomainRestrictedOnClass() {
            super();
        }
        public DataDomainRestrictedOnClass(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnClass(OWLDataProperty subject, OWLClass value) {
            super(subject, value);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_CLASS);
        }
    }

    /**
     * This class combines {@link ApplyingDataMinRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within a minimum number of data properties in a given data type.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_MIN_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnMinData
            extends ApplyingDataMinRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLDataRange> {
        public DataDomainRestrictedOnMinData() {
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnMinData(int cardinality) {
            super(cardinality);
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public DataDomainRestrictedOnMinData(OWLDataMinCardinality restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject, OWLDataMinCardinality restriction) {
            super(subject, restriction);
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public DataDomainRestrictedOnMinData(OWLDataProperty subject, OWLDataRange value) {
            super(subject, value);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_MIN_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataMaxRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within a maximum number of data properties in a given data type.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_MAX_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnMaxData
            extends ApplyingDataMaxRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLDataRange> {
        public DataDomainRestrictedOnMaxData() {
            super();
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnMaxData(int cardinality) {
            super(cardinality);
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public DataDomainRestrictedOnMaxData(OWLDataMaxCardinality restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnMaxData(OWLDataProperty subject, OWLDataMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_MAX_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataExactRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within an exact number of data properties in a given data type.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_EXACT_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnExactData
            extends ApplyingDataExactRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLDataRange> {
        public DataDomainRestrictedOnExactData() {
            super();
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnExactData(int cardinality) {
            super(cardinality);
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public DataDomainRestrictedOnExactData(OWLDataExactCardinality restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnExactData(OWLDataProperty subject, OWLDataExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_EXACT_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataSomeRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within an existential cardinality of data properties in a given data type.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_SOME_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnSomeData
            extends ApplyingDataSomeRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLDataRange> {
        public DataDomainRestrictedOnSomeData() {
            super();
        }
        public DataDomainRestrictedOnSomeData(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnSomeData(OWLDataProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnSomeData(OWLDataProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnSomeData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnSomeData(OWLDataSomeValuesFrom restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnSomeData(OWLDataProperty subject, OWLDataSomeValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_SOME_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataAllRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within an universal cardinality of data properties in a given data type.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_ALL_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnAllData
            extends ApplyingDataAllRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLDataRange> {
        public DataDomainRestrictedOnAllData() {
            super();
        }
        public DataDomainRestrictedOnAllData(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnAllData(OWLDataProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnAllData(OWLDataProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnAllData(OWLDataProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnAllData(OWLDataAllValuesFrom restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnAllData(OWLDataProperty subject, OWLDataAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_ALL_DATA);
        }
    }

    /**
     * This class combines {@link ApplyingObjectMinRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within a minimum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_MIN_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnMinObject
            extends ApplyingObjectMinRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLClass> {
        public DataDomainRestrictedOnMinObject() {
            super();
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject, OWLClass value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnMinObject(int cardinality) {
            super(cardinality);
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public DataDomainRestrictedOnMinObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public DataDomainRestrictedOnMinObject(OWLObjectMinCardinality restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnMinObject(OWLDataProperty subject, OWLObjectMinCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_MIN_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectMaxRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within a maximum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_MAX_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnMaxObject
            extends ApplyingObjectMaxRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLClass> {
        public DataDomainRestrictedOnMaxObject() {
            super();
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject, OWLClass value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnMaxObject(int cardinality) {
            super(cardinality);
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public DataDomainRestrictedOnMaxObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public DataDomainRestrictedOnMaxObject(OWLObjectMaxCardinality restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnMaxObject(OWLDataProperty subject, OWLObjectMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_MAX_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectExactRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within an exact number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_EXACT_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnExactObject
            extends ApplyingObjectExactRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLClass> {
        public DataDomainRestrictedOnExactObject() {
            super();
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject, OWLClass value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnExactObject(int cardinality) {
            super(cardinality);
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public DataDomainRestrictedOnExactObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public DataDomainRestrictedOnExactObject(OWLObjectExactCardinality restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnExactObject(OWLDataProperty subject, OWLObjectExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_EXACT_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectSomeRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within an existential cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_SOME_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnSomeObject
            extends ApplyingObjectSomeRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLClass> {
        public DataDomainRestrictedOnSomeObject() {
            super();
        }
        public DataDomainRestrictedOnSomeObject(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnSomeObject(OWLDataProperty subject, OWLClass value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnSomeObject(OWLDataProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnSomeObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnSomeObject(OWLObjectSomeValuesFrom restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnSomeObject(OWLDataProperty subject, OWLObjectSomeValuesFrom restriction) {
            super(subject, restriction);
        }


        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_SOME_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectSomeRestriction} with the {@link RestrictOnDataPropertyDomain}
     * interface in order to define a restriction of a data property domain within an universal cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_DOMAIN_ALL_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataDomainRestrictedOnAllObject
            extends ApplyingObjectAllRestriction<OWLDataProperty,OWLDataPropertyDomainAxiom>
            implements RestrictOnDataPropertyDomain<OWLClass>{
        public DataDomainRestrictedOnAllObject() {
            super();
        }
        public DataDomainRestrictedOnAllObject(OWLDataProperty subject) {
            super(subject);
        }
        public DataDomainRestrictedOnAllObject(OWLDataProperty subject, OWLClass value) {
            super(subject, value);
        }
        public DataDomainRestrictedOnAllObject(OWLDataProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public DataDomainRestrictedOnAllObject(OWLDataProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public DataDomainRestrictedOnAllObject(OWLObjectAllValuesFrom restriction) {
            super(restriction);
        }
        public DataDomainRestrictedOnAllObject(OWLDataProperty subject, OWLObjectAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_DOMAIN_ALL_OBJECT);
        }
    }




    /**
     * This interface implements the {@link #getAxiom(OWLReferencesInterface)}
     * for range restrictions that involve an {@link OWLDataProperty} as {@code subject}.
     * It is based on {@link OWLDataFactory#getOWLDataPropertyRangeAxiom(OWLDataPropertyExpression, OWL2Datatype)}.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLDataPropertyRangeAxiom( {@link #getSubject()}, {@link #getRestriction(OWLReferencesInterface)})},
     * in order to link the restriction range to the specified subject in the ontology.
     */
    interface  RestrictOnDataPropertyRange
            extends SemanticRestriction<OWLDataProperty,OWLDataPropertyRangeAxiom,OWLDataRange>{
        @Override
        default OWLDataPropertyRangeAxiom getAxiom(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLDataPropertyRangeAxiom(getSubject(), getValue());
        }
    }

    /**
     * This class combines {@link ApplyingRestriction} with the {@link RestrictOnDataPropertyRange}
     * interface in order to define a restriction of a data property range within an universal data type.
     * It set the {@link #restrictionType} to {@link RestrictionType#DATA_RANGE_ALL_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class DataRangeRestricted
            extends ApplyingRestriction<OWLDataProperty,OWLDataPropertyRangeAxiom,OWLDataRange>
            implements RestrictOnDataPropertyRange{
        public DataRangeRestricted() {
            super();
        }
        public DataRangeRestricted(OWLDataProperty subject) {
            super(subject);
        }
        public DataRangeRestricted(OWLDataProperty subject, OWLDataRange value) {
            super(subject, value);
        }

        @Override @Deprecated
        public OWLClassExpression getRestriction(OWLReferencesInterface ontology) {
            return null; // owl api does not support this
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.DATA_RANGE_ALL_DATA);
        }
    }


    /**
     * This interface implements the {@link #getAxiom(OWLReferencesInterface)}
     * for domain restrictions that involve an {@link OWLObjectProperty} as {@code subject}.
     * It is based on {@link OWLDataFactory#getOWLObjectPropertyDomainAxiom(OWLObjectPropertyExpression, OWLClassExpression)}.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLObjectPropertyDomainAxiom( {@link #getSubject()}, {@link #getRestriction(OWLReferencesInterface)})},
     * in order to link the restriction domain to the specified subject in the ontology.
     * @param <V> the type of the value of the restriction.
     */
    interface RestrictOnObjectPropertyDomain<V extends OWLObject>
            extends SemanticRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom,V>{
        @Override
        default OWLObjectPropertyDomainAxiom getAxiom(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLObjectPropertyDomainAxiom(getSubject(), getRestriction(ontology));
        }
    }

    /**
     * This class combines {@link ApplyingClassRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define equivalence restriction between an object property domain and a class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_CLASS}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnClass
            extends ApplyingClassRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLClass> {
        public ObjectDomainRestrictedOnClass() {
            super();
        }
        public ObjectDomainRestrictedOnClass(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnClass(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_CLASS);
        }
    }
    
    /**
     * This class combines {@link ApplyingDataMinRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within a minimum number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_MIN_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnMinData
            extends ApplyingDataMinRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLDataRange> {
        public ObjectDomainRestrictedOnMinData() {
            super();
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnMinData(int cardinality) {
            super(cardinality);
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectDomainRestrictedOnMinData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectDomainRestrictedOnMinData(OWLDataMinCardinality restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnMinData(OWLObjectProperty subject, OWLDataMinCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_MIN_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataMaxRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within a maximum number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_MAX_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnMaxData
            extends ApplyingDataMaxRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLDataRange> {
        public ObjectDomainRestrictedOnMaxData() {
            super();
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnMaxData(int cardinality) {
            super(cardinality);
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectDomainRestrictedOnMaxData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectDomainRestrictedOnMaxData(OWLDataMaxCardinality restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnMaxData(OWLObjectProperty subject, OWLDataMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_MAX_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataExactRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within an exact number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_EXACT_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnExactData
            extends ApplyingDataExactRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLDataRange> {
        public ObjectDomainRestrictedOnExactData() {
            super();
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnExactData(int cardinality) {
            super(cardinality);
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectDomainRestrictedOnExactData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectDomainRestrictedOnExactData(OWLDataExactCardinality restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnExactData(OWLObjectProperty subject, OWLDataExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_EXACT_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataSomeRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within an existential cardinality of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_SOME_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnSomeData
            extends ApplyingDataSomeRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLDataRange>{
        public ObjectDomainRestrictedOnSomeData() {
            super();
        }
        public ObjectDomainRestrictedOnSomeData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnSomeData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnSomeData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnSomeData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnSomeData(OWLDataSomeValuesFrom restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnSomeData(OWLObjectProperty subject, OWLDataSomeValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_SOME_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataAllRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within an universal cardinality of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_ALL_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnAllData
            extends ApplyingDataAllRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLDataRange> {
        public ObjectDomainRestrictedOnAllData() {
            super();
        }
        public ObjectDomainRestrictedOnAllData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnAllData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnAllData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnAllData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnAllData(OWLDataAllValuesFrom restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnAllData(OWLObjectProperty subject, OWLDataAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_ALL_DATA);
        }
    }

    /**
     * This class combines {@link ApplyingObjectMinRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within a minimum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_MIN_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnMinObject
            extends ApplyingObjectMinRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLClass> {
        public ObjectDomainRestrictedOnMinObject() {
            super();
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnMinObject(int cardinality) {
            super(cardinality);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectMinCardinality restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnMinObject(OWLObjectProperty subject, OWLObjectMinCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_MIN_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectMaxRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within a maximum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_MAX_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnMaxObject
            extends ApplyingObjectMaxRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLClass> {
        public ObjectDomainRestrictedOnMaxObject() {
            super();
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnMaxObject(int cardinality) {
            super(cardinality);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectMaxCardinality restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnMaxObject(OWLObjectProperty subject, OWLObjectMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_MAX_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectExactRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within an existential cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_EXACT_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnExactObject
            extends ApplyingObjectExactRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLClass>{
        public ObjectDomainRestrictedOnExactObject() {
            super();
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnExactObject(int cardinality) {
            super(cardinality);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectExactCardinality restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnExactObject(OWLObjectProperty subject, OWLObjectExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_EXACT_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectSomeRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within an existential cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_SOME_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnSomeObject
            extends ApplyingObjectSomeRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLClass> {
        public ObjectDomainRestrictedOnSomeObject() {
            super();
        }
        public ObjectDomainRestrictedOnSomeObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnSomeObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnSomeObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnSomeObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnSomeObject(OWLObjectSomeValuesFrom restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnSomeObject(OWLObjectProperty subject, OWLObjectSomeValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_SOME_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectAllRestriction} with the {@link RestrictOnObjectPropertyDomain}
     * interface in order to define a restriction of an object property domain within an universal cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_DOMAIN_ALL_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectDomainRestrictedOnAllObject
            extends ApplyingObjectAllRestriction<OWLObjectProperty,OWLObjectPropertyDomainAxiom>
            implements RestrictOnObjectPropertyDomain<OWLClass>{
        public ObjectDomainRestrictedOnAllObject() {
            super();
        }
        public ObjectDomainRestrictedOnAllObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectDomainRestrictedOnAllObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectDomainRestrictedOnAllObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectDomainRestrictedOnAllObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectDomainRestrictedOnAllObject(OWLObjectAllValuesFrom restriction) {
            super(restriction);
        }
        public ObjectDomainRestrictedOnAllObject(OWLObjectProperty subject, OWLObjectAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_DOMAIN_ALL_OBJECT);
        }
    }



    /**
     * This interface implements the {@link #getAxiom(OWLReferencesInterface)}
     * for range restrictions that involve an {@link OWLObjectProperty} as {@code subject}.
     * It is based on {@link OWLDataFactory#getOWLObjectPropertyRangeAxiom(OWLObjectPropertyExpression, OWLClassExpression)}.
     * Indeed it calls: {@code ontology.getOWLFactory().getOWLObjectPropertyRangeAxiom( {@link #getSubject()}, {@link #getRestriction(OWLReferencesInterface)})},
     * in order to link the restriction range to the specified subject in the ontology.
     * @param <V> the type of the value of the restriction.
     */
    interface RestrictOnObjectPropertyRange<V extends OWLObject>
            extends SemanticRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom,V>{
        @Override
        default OWLObjectPropertyRangeAxiom getAxiom(OWLReferencesInterface ontology) {
            return ontology.getOWLFactory().getOWLObjectPropertyRangeAxiom(getSubject(), getRestriction(ontology));
        }
    }

    /**
     * This class combines {@link ApplyingClassRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define equivalence restriction between an object property range and a class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_CLASS}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnClass
            extends ApplyingClassRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLClass> {
        public ObjectRangeRestrictedOnClass() {
            super();
        }
        public ObjectRangeRestrictedOnClass(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnClass(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_CLASS);
        }
    }

    /**
     * This class combines {@link ApplyingDataMinRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within a minimum number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_MIN_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnMinData
            extends ApplyingDataMinRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLDataRange> {
        public ObjectRangeRestrictedOnMinData() {
            super();
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnMinData(int cardinality) {
            super(cardinality);
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectRangeRestrictedOnMinData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectRangeRestrictedOnMinData(OWLDataMinCardinality restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnMinData(OWLObjectProperty subject, OWLDataMinCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_MIN_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataMaxRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within a maximum number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_MAX_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnMaxData
            extends ApplyingDataMaxRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLDataRange> {
        public ObjectRangeRestrictedOnMaxData() {
            super();
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnMaxData(int cardinality) {
            super(cardinality);
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectRangeRestrictedOnMaxData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectRangeRestrictedOnMaxData(OWLDataMaxCardinality restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnMaxData(OWLObjectProperty subject, OWLDataMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_MAX_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataExactRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within an exact number of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_EXACT_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnExactData
            extends ApplyingDataExactRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLDataRange> {
        public ObjectRangeRestrictedOnExactData() {
            super();
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnExactData(int cardinality) {
            super(cardinality);
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectRangeRestrictedOnExactData(OWLDataProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject, OWLDataProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectRangeRestrictedOnExactData(OWLDataExactCardinality restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnExactData(OWLObjectProperty subject, OWLDataExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_EXACT_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataSomeRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within an existential cardinality of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_SOME_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnSomeData
            extends ApplyingDataSomeRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLDataRange>{
        public ObjectRangeRestrictedOnSomeData() {
            super();
        }
        public ObjectRangeRestrictedOnSomeData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnSomeData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnSomeData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnSomeData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnSomeData(OWLDataSomeValuesFrom restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnSomeData(OWLObjectProperty subject, OWLDataSomeValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_SOME_DATA);
        }
    }
    /**
     * This class combines {@link ApplyingDataAllRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within an universal cardinality of data properties in a given data range.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_ALL_DATA}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnAllData
            extends ApplyingDataAllRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLDataRange> {
        public ObjectRangeRestrictedOnAllData() {
            super();
        }
        public ObjectRangeRestrictedOnAllData(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnAllData(OWLObjectProperty subject, OWLDataRange value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnAllData(OWLObjectProperty subject, OWLDataProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnAllData(OWLObjectProperty subject, OWLDataRange value, OWLDataProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnAllData(OWLDataAllValuesFrom restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnAllData(OWLObjectProperty subject, OWLDataAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_ALL_DATA);
        }
    }

    /**
     * This class combines {@link ApplyingObjectMinRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within a minimum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_MIN_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnMinObject
            extends ApplyingObjectMinRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLClass> {
        public ObjectRangeRestrictedOnMinObject() {
            super();
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnMinObject(int cardinality) {
            super(cardinality);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectMinCardinality restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnMinObject(OWLObjectProperty subject, OWLObjectMinCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_MIN_OBJECT);
        }
    }
    /**
     * This class combines {@link ObjectRangeRestrictedOnMaxObject} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within a maximum number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_MAX_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnMaxObject
            extends ApplyingObjectMaxRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLClass> {
        public ObjectRangeRestrictedOnMaxObject() {
            super();
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnMaxObject(int cardinality) {
            super(cardinality);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectMaxCardinality restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnMaxObject(OWLObjectProperty subject, OWLObjectMaxCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_MAX_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectExactRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within an exact number of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_EXACT_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnExactObject
            extends ApplyingObjectExactRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLClass> {
        public ObjectRangeRestrictedOnExactObject() {
            super();
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnExactObject(int cardinality) {
            super(cardinality);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value, int cardinality) {
            super(subject, value, cardinality);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty property, int cardinality) {
            super(property, cardinality);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject, OWLObjectProperty property, int cardinality) {
            super(subject, property, cardinality);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property, int cardinality) {
            super(subject, value, property, cardinality);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectExactCardinality restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnExactObject(OWLObjectProperty subject, OWLObjectExactCardinality restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_EXACT_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectSomeRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within an existential cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_SOME_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnSomeObject
            extends ApplyingObjectSomeRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLClass> {
        public ObjectRangeRestrictedOnSomeObject() {
            super();
        }
        public ObjectRangeRestrictedOnSomeObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnSomeObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnSomeObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnSomeObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnSomeObject(OWLObjectSomeValuesFrom restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnSomeObject(OWLObjectProperty subject, OWLObjectSomeValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_SOME_OBJECT);
        }
    }
    /**
     * This class combines {@link ApplyingObjectAllRestriction} with the {@link RestrictOnObjectPropertyRange}
     * interface in order to define a restriction of an object property range within an universal cardinality of object properties in a given class.
     * It set the {@link #restrictionType} to {@link RestrictionType#OBJECT_RANGE_ALL_OBJECT}.
     * It does not implement any further features rather than the constructors, see the above classes for more details.
     */
    class ObjectRangeRestrictedOnAllObject
            extends ApplyingObjectAllRestriction<OWLObjectProperty,OWLObjectPropertyRangeAxiom>
            implements RestrictOnObjectPropertyRange<OWLClass> {
        public ObjectRangeRestrictedOnAllObject() {
            super();
        }
        public ObjectRangeRestrictedOnAllObject(OWLObjectProperty subject) {
            super(subject);
        }
        public ObjectRangeRestrictedOnAllObject(OWLObjectProperty subject, OWLClass value) {
            super(subject, value);
        }
        public ObjectRangeRestrictedOnAllObject(OWLObjectProperty subject, OWLObjectProperty property) {
            super(subject, property);
        }
        public ObjectRangeRestrictedOnAllObject(OWLObjectProperty subject, OWLClass value, OWLObjectProperty property) {
            super(subject, value, property);
        }
        public ObjectRangeRestrictedOnAllObject(OWLObjectAllValuesFrom restriction) {
            super(restriction);
        }
        public ObjectRangeRestrictedOnAllObject(OWLObjectProperty subject, OWLObjectAllValuesFrom restriction) {
            super(subject, restriction);
        }

        @Override
        protected void setRestrictionType() {
            this.restrictionType = new RestrictionType( RestrictionType.OBJECT_RANGE_ALL_OBJECT);
        }
    }

}
