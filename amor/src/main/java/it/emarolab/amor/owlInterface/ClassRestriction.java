package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import org.semanticweb.owlapi.model.*;

import static it.emarolab.amor.owlInterface.OWLManipulator.*;

// todo check documentation

/**
 * This is a container for quantify class restrictions.
 * Also, it can be produced fom {@link OWLEnquirer#getClassRestrictions(OWLClass)} and
 * describes {@link OWLClassExpression} of the given class, if its type is one of:
 * {@link ClassExpressionType#OBJECT_SOME_VALUES_FROM},  {@link ClassExpressionType#DATA_SOME_VALUES_FROM} (&exist;),
 * {@link ClassExpressionType#OBJECT_ALL_VALUES_FROM}, {@link ClassExpressionType#DATA_ALL_VALUES_FROM} (&forall;),
 * {@link ClassExpressionType#OBJECT_MIN_CARDINALITY}, {@link ClassExpressionType#DATA_MIN_CARDINALITY},
 * {@link ClassExpressionType#OBJECT_MAX_CARDINALITY}, {@link ClassExpressionType#DATA_MAX_CARDINALITY},
 * {@link ClassExpressionType#OBJECT_EXACT_CARDINALITY}, {@link ClassExpressionType#DATA_EXACT_CARDINALITY}.
 * Where the getters of this containers depends from one of those types, assigned through setters.
 */
public class ClassRestriction {
    private OWLClass definitionOf;
    private OWLProperty property;
    private Boolean isDataProperty;
    private int expressioneType, cardinality;
    private OWLClass object;
    private OWLDataRange data;

    /**
     * Object used to log information about this class instances.
     * Logs are activated by flag: {@link Logger.LoggerFlag#LOG_OWL_ENQUIRER}
     */
    //private Logger logger = new Logger(this, Logger.LoggerFlag.getLogOWLEnquirer() | Logger.LoggerFlag.getLogOWLManipulator());

    /**
     * Create a class restriction for a data property and set its
     * domain to be an {@link OWLDataRange} (see {@link #getDataTypeRestriction()}).
     * @param subject the class restricted by this axiom.
     * @param property the data property that restricts the class
     */
    public ClassRestriction(OWLClass subject, OWLDataProperty property) {
        this.definitionOf = subject;
        this.property = property;
        this.isDataProperty = true;
    }
    /**
     * Create a class restriction for an object property and set its
     * domain to be an {@link OWLClass} (see {@link #getObjectRestriction()}).
     * @param subject the class restricted by this axiom.
     * @param property the object property that restricts the class
     */
    public ClassRestriction(OWLClass subject, OWLObjectProperty property) {
        this.definitionOf = subject;
        this.property = property;
        this.isDataProperty = false;
    }

    /**
     * Initialise to describe universal data property restriction over
     * a data type (supported: {@link OWLDataFactory#getStringOWLDatatype()},
     * {@link OWLDataFactory#getDoubleOWLDatatype()}, {@link OWLDataFactory#getFloatOWLDatatype()},
     * {@link OWLDataFactory#getIntegerOWLDatatype()} and
     * {@code ontoRef.getOWLFactory().getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI())}).
     * In symbols: {@code class &forall; hasDataProperty dataType}.
     * @param dataType the range of data of the universal restriction.
     */
    public void setDataOnlyRestriction( OWLDataRange dataType){
        if ( isDataProperty != null){
            if( isDataProperty) {
                this.expressioneType = RESTRICTION_ONLY;
                this.data = dataType;
            } //// else logger.addDebugString( "Cannot set a 'only' data restriction over an object property", true);
        } //// else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe existential data property restriction over
     * a data type (supported: {@link OWLDataFactory#getStringOWLDatatype()},
     * {@link OWLDataFactory#getDoubleOWLDatatype()}, {@link OWLDataFactory#getFloatOWLDatatype()},
     * {@link OWLDataFactory#getIntegerOWLDatatype()} and
     * {@code ontoRef.getOWLFactory().getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI())}).
     * In symbols: {@code class &exists; hasDataProperty dataType}.
     * @param dataType the range of data of the existential restriction.
     */
    public void setDataSomeRestriction( OWLDataRange dataType){
        if( isDataProperty != null) {
            if (isDataProperty) {
                this.expressioneType = RESTRICTION_SOME;
                this.data = dataType;
            } // else logger.addDebugString("Cannot set a 'some' data restriction over an object property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe a data property with a minimal cardinality restriction over
     * a data type (supported: {@link OWLDataFactory#getStringOWLDatatype()},
     * {@link OWLDataFactory#getDoubleOWLDatatype()}, {@link OWLDataFactory#getFloatOWLDatatype()},
     * {@link OWLDataFactory#getIntegerOWLDatatype()} and
     * {@code ontoRef.getOWLFactory().getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI())}).
     * In symbols: {@code class &lt;<sub>d</sub> hasDataProperty dataType}.
     * @param cardinality the cardinality of the restriction {@code d}.
     * @param dataType the range of data of the minimal cardinality restriction.
     */
    public void setDataMinRestriction( int cardinality, OWLDataRange dataType){
        if( isDataProperty != null){
            if( isDataProperty) {
                this.expressioneType = RESTRICTION_MIN;
                this.cardinality = cardinality;
                this.data = dataType;
            } // else logger.addDebugString( "Cannot set a 'min' data restriction over an object property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe a data property with a maximal cardinality restriction over
     * a data type (supported: {@link OWLDataFactory#getStringOWLDatatype()},
     * {@link OWLDataFactory#getDoubleOWLDatatype()}, {@link OWLDataFactory#getFloatOWLDatatype()},
     * {@link OWLDataFactory#getIntegerOWLDatatype()} and
     * {@code ontoRef.getOWLFactory().getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI())}).
     * In symbols: {@code class &gt;<sub>d</sub> hasDataProperty dataType}.
     * @param cardinality the cardinality of the restriction {@code d}.
     * @param dataType the range of data of the maximal cardinality restriction.
     */
    public void setDataMaxRestriction(int cardinality, OWLDataRange dataType){
        if( isDataProperty != null){
            if( isDataProperty) {
                this.expressioneType = RESTRICTION_MAX;
                this.cardinality = cardinality;
                this.data = dataType;
            } // else logger.addDebugString( "Cannot set a 'max' data restriction over an object property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe a data property with an exact cardinality restriction over
     * a data type (supported: {@link OWLDataFactory#getStringOWLDatatype()},
     * {@link OWLDataFactory#getDoubleOWLDatatype()}, {@link OWLDataFactory#getFloatOWLDatatype()},
     * {@link OWLDataFactory#getIntegerOWLDatatype()} and
     * {@code ontoRef.getOWLFactory().getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI())}).
     * In symbols: {@code class =<sub>d</sub> hasDataProperty dataType}.
     * @param cardinality the cardinality of the restriction {@code d}.
     * @param dataType the range of data of the exact cardinality restriction.
     */
    public void setDataExactRestriction( int cardinality, OWLDataRange dataType){
        if( isDataProperty != null){
            if( isDataProperty) {
                this.expressioneType = RESTRICTION_EXACT;
                this.cardinality = cardinality;
                this.data = dataType;
            } // else logger.addDebugString( "Cannot set a 'exact' data restriction over an object property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }

    /**
     * Initialise to describe universal object property restriction over
     * a {@link OWLClass}.
     * In symbols: {@code classSubject &forall; hasObjectProperty classObject}.
     * @param object the class domain of the universal property restriction.
     */
    public void setObjectOnlyRestriction( OWLClass object){
        if( isDataProperty != null) {
            if (!isDataProperty) {
                this.expressioneType = RESTRICTION_ONLY;
                this.object = object;
            } // else logger.addDebugString("Cannot set a 'only' object restriction over a data property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe existential object property restriction over
     * a {@link OWLClass}.
     * In symbols: {@code classSubject &exists; hasObjectProperty classObject}.
     * @param object the class domain of the existential property restriction.
     */
    public void setObjectSomeRestriction( OWLClass object){
        if( isDataProperty != null){
            if( ! isDataProperty) {
                this.expressioneType = RESTRICTION_SOME;
                this.object = object;
            } // else logger.addDebugString( "Cannot set a 'some' object restriction over a data property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe an object property with a minimal cardinality restriction over
     * a {@link OWLClass}.
     * In symbols: {@code classSubject &lt;<sub>d</sub> hasObjectProperty objectClass}.
     * @param cardinality the cardinality of the restriction {@code d}.
     * @param object the class domain of the minimal cardinality restriction over the property.
     */
    public void setObjectMinRestriction(int cardinality, OWLClass object){
        if( isDataProperty != null){
            if( ! isDataProperty) {
                this.expressioneType = RESTRICTION_MIN;
                this.cardinality = cardinality;
                this.object = object;
            } // else logger.addDebugString( "Cannot set an 'min' object restriction over a data property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe an object property with a maximal cardinality restriction over
     * a {@link OWLClass}.
     * In symbols: {@code classSubject &gt;<sub>d</sub> hasObjectProperty objectClass}.
     * @param cardinality the cardinality of the restriction {@code d}.
     * @param object the class domain of the maximal cardinality restriction over the property.
     */
    public void setObjectMaxRestriction(int cardinality, OWLClass object){
        if( isDataProperty != null){
            if( ! isDataProperty) {
                this.expressioneType = RESTRICTION_MAX;
                this.cardinality = cardinality;
                this.object = object;
            } // else logger.addDebugString( "Cannot set an 'max' object restriction over a data property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }
    /**
     * Initialise to describe an object property with a exact cardinality restriction over
     * a {@link OWLClass}.
     * In symbols: {@code classSubject =<sub>d</sub> hasObjectProperty objectClass}.
     * @param cardinality the cardinality of the restriction {@code d}.
     * @param object the class domain of the exact cardinality restriction over the property.
     */
    public void setObjectExactRestriction(int cardinality, OWLClass object){
        if( isDataProperty != null){
            if( ! isDataProperty) {
                this.expressioneType = RESTRICTION_EXACT;
                this.cardinality = cardinality;
                this.object = object;
            } // else logger.addDebugString( "Cannot set an 'exact' object restriction over a data property", true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
    }

    /**
     * It returns {@code null} if {@code ( ! {@link #isDataProperty} )}.
     * Otherwise, it returns the data type of the data property restriction.
     * @return the data type of the data property restriction.
     */
    public OWLDataRange getDataTypeRestriction(){
        if ( isDataProperty != null){
            if( isDataProperty)
                return data;
            // else logger.addDebugString( "a class restriction based on an object property does not have data restrictions.");
        } // else logger.addDebugString("Null property, nothing has been done!", true);
        return null;
    }

    /**
     * It returns {@code null} if {@code ( {@link #isDataProperty} )}.
     * Otherwise, it returns the OWL class of the data property restriction.
     * @return the class of the data property restriction.
     */
    public OWLClass getObjectRestriction(){
        if( isDataProperty != null){
            if( ! isDataProperty)
                return object;
            //logger.addDebugString( "a class restriction based on a data property does not have object restrictions.");
        } // else logger.addDebugString("Null property, nothing has been done!", true);
        return null;
    }

    /**
     * Returns {@code null} if the expression type is RESTRICTION_ONLY or RESTRICTION_SOME.
     * Otherwise, it return the cardinality of the data property restriction.
     * @return the cardinality of the data propriety restriction.
     */
    public Integer getCardinality(){
        if( expressioneType >= RESTRICTION_MIN)
            return cardinality;
        //logger.addDebugString( getExpressionTypeName() + " does not have a cardinality.", true);
        return null;
    }

    /**
     * @return the property of this restriction, defining the
     * given class (see {@link #isDefinitionOf()}).
     */
    public OWLProperty getProperty() {
        return property;
    }

    /**
     * @return {@link #getProperty()}, casted as {@link OWLDataProperty},
     * if {@code this} {@link #isDataProperty} is true.
     * {@code Null} otherwise.
     */
    public OWLDataProperty getDataProperty() {
        if( isDataProperty != null){
            if( isDataProperty)
                return (OWLDataProperty) property;
            //logger.addDebugString( "cannot assign data to object property over: " + OWLReferencesInterface.getOWLName( property), true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
        return null;
    }
    /**
     * @return {@link #getProperty()}, casted as {@link OWLObjectProperty},
     * if {@code this} {@link #isDataProperty} is false.
     * {@code Null} otherwise.
     */
    public OWLObjectProperty getObjectProperty() {
        if( isDataProperty != null){
            if( ! isDataProperty)
                return (OWLObjectProperty) property;
            //logger.addDebugString( "cannot assign data to object property over: " + OWLReferencesInterface.getOWLName( property), true);
        } // else logger.addDebugString("Null property, nothing has been done!", true);
        return null;
    }

    /**
     * Return the subject of the class restriction.
     * It is the same class given as input to {@link OWLEnquirer#getClassRestrictions(OWLClass)}
     * and produce this container. In the basic set given by {@link OWLEnquirer#getClassRestrictions(OWLClass)}
     * this field is always the same.
     * @return the class that is defined also by this property restriction.
     */
    public OWLClass isDefinitionOf() {
        return definitionOf;
    }

    /**
     * It is used to discriminate this instance for being:
     * {@link OWLManipulator#RESTRICTION_SOME}, {@link OWLManipulator#RESTRICTION_ONLY},
     * {@link OWLManipulator#RESTRICTION_MIN}, {@link OWLManipulator#RESTRICTION_EXACT}
     * or {@link OWLManipulator#RESTRICTION_MAX}.
     * @return the identifier of the expression type.
     */
    public int getExpressiontType() {
        return expressioneType;
    }

    /**
     * @return a string identifying the actual {@link #getExpressiontType()}.
     */
    public String getExpressionTypeName(){
        if( isSomeRestriction())
            return "<some>";
        if( isOnlyRestriction())
            return "<only>";
        if( isMinRestriction())
            return "<min>";
        if( isExactRestriction())
            return "<exact>";
        if( isMaxRestriction())
            return "<max>";
        return "<null>"; // should not happen
    }

    /**
     * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_SOME}
     */
    public boolean isSomeRestriction(){
        return expressioneType == RESTRICTION_SOME;
    }
    /**
     * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_ONLY}
     */
    public boolean isOnlyRestriction(){
        return expressioneType == RESTRICTION_ONLY;
    }
    /**
     * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_MIN}
     */
    public boolean isMinRestriction(){
        return expressioneType == RESTRICTION_MIN;
    }
    /**
     * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_EXACT}
     */
    public boolean isExactRestriction(){
        return expressioneType == RESTRICTION_EXACT;
    }
    /**
     * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_MAX}
     */
    public boolean isMaxRestriction(){
        return expressioneType == RESTRICTION_MAX;
    }

    /**
     * @return {@code true} if this object has been instantiated
     * with {@link #ClassRestriction(OWLClass, OWLDataProperty)}.
     * {@code False} otherwise.
     */
    public boolean restrictsOverDataProperty(){
        return isDataProperty;
    }
    /**
     * @return {@code true} if this object has been instantiated
     * with {@link #ClassRestriction(OWLClass, OWLObjectProperty)}.
     * {@code False} otherwise.
     */
    public boolean restrictsOverObjectProperty(){
        return ! isDataProperty;
    }

    /**
     * @return a short name for {@link #isDefinitionOf()}.
     */
    public String getDefinitionOfName(){
        return OWLReferencesInterface.getOWLName(  definitionOf);
    }

    /**
     * @return a short name for {@link #getProperty()}.
     */
    public String getPropertyName(){
        return OWLReferencesInterface.getOWLName(  property);
    }

    /**
     * @return a short name for {@link #getDataTypeRestriction()}
     * if .{@link #isDataProperty}. Otherwise, a short name for
     * {@link #getObjectRestriction()}.
     */
    public String getObjectName(){
        if( isDataProperty != null){
            if( isDataProperty) {
                try{
                    return data.toString().substring( data.toString().lastIndexOf('#') + 1, data.toString().length());
                } catch (Exception e) {
                    return data + "";
                }
            }
            return OWLReferencesInterface.getOWLName( object);
        } // else logger.addDebugString("Null property, cannot give object name!", true);
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassRestriction)) return false;

        ClassRestriction that = (ClassRestriction) o;

        if (expressioneType != that.expressioneType) return false;
        if (cardinality != that.cardinality) return false;
        if (definitionOf != null ? !definitionOf.equals(that.definitionOf) : that.definitionOf != null)
            return false;
        if (property != null ? !property.equals(that.property) : that.property != null) return false;
        if (isDataProperty != null ? !isDataProperty.equals(that.isDataProperty) : that.isDataProperty != null)
            return false;
        if (object != null ? !object.equals(that.object) : that.object != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;
    }
    @Override
    public int hashCode() {
        int result = definitionOf != null ? definitionOf.hashCode() : 0;
        result = 31 * result + (property != null ? property.hashCode() : 0);
        result = 31 * result + (isDataProperty != null ? isDataProperty.hashCode() : 0);
        result = 31 * result + expressioneType;
        result = 31 * result + cardinality;
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String out = "Class definition: " + getDefinitionOfName() + " " + getPropertyName();
        if( isDataProperty != null) {
            if (isDataProperty)
                out += " data::";
            else out += " object::";
        } else out = " null::";
        out += " " + getExpressionTypeName();
        if( expressioneType >= RESTRICTION_MIN)
            out += " " + getCardinality();
        out += " " + getObjectName();
        return out;
    }
}
