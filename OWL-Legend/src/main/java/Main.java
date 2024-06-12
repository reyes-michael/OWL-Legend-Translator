import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.File;
import java.util.Set;


public class Main {

    public static void main(String[] args) throws Exception {
        // Create an OWLOntologyManager to load the ontology
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        // Load the ontology from a file
        File file = new File("OWL-Legend/src/main/resources/pizza.owl");
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

        // Get the data factory to create OWL objects
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        // Define the IRI of the class you want to extract
        IRI classIRI = IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#American");
        OWLClass americanPizzaClass = dataFactory.getOWLClass(classIRI);

        // Extract and display class details
        System.out.println("Class: " + americanPizzaClass);
        printClassDetails(ontology, americanPizzaClass, dataFactory);
    }

    private static void printClassDetails(OWLOntology ontology, OWLClass owlClass, OWLDataFactory dataFactory) {
        // Print superclasses
        printSuperClasses(ontology, owlClass);

        // Print subclass restrictions and object properties
        printRestrictionsAndObjectProperties(ontology, owlClass, dataFactory);

        // Print labels
        printLabels(ontology, owlClass);
    }

    private static void printSuperClasses(OWLOntology ontology, OWLClass owlClass) {
        Set<OWLSubClassOfAxiom> subclassAxioms = ontology.getSubClassAxiomsForSubClass(owlClass);

        for (OWLSubClassOfAxiom axiom : subclassAxioms) {
            OWLClassExpression superClass = axiom.getSuperClass();
            if (superClass.isAnonymous()) {
                System.out.println("Anonymous Superclass: " + superClass);
            } else {
                System.out.println("Superclass: " + superClass.asOWLClass().getIRI());
            }
        }
    }

    private static void printRestrictionsAndObjectProperties(OWLOntology ontology, OWLClass owlClass, OWLDataFactory dataFactory) {
        Set<OWLSubClassOfAxiom> subclassAxioms = ontology.getSubClassAxiomsForSubClass(owlClass);

        for (OWLSubClassOfAxiom axiom : subclassAxioms) {
            OWLClassExpression superClass = axiom.getSuperClass();
            if (superClass instanceof OWLRestriction) {
                OWLRestriction restriction = (OWLRestriction) superClass;
                if (restriction instanceof OWLQuantifiedRestriction) {
                    OWLQuantifiedRestriction quantifiedRestriction = (OWLQuantifiedRestriction) restriction;
                    OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) quantifiedRestriction.getProperty();
                    OWLClassExpression filler = (OWLClassExpression) quantifiedRestriction.getFiller();

                    System.out.println("  Property: " + property.asOWLObjectProperty().getIRI());
                    if (quantifiedRestriction instanceof OWLObjectSomeValuesFrom) {
                        System.out.println("    Restriction: someValuesFrom");
                    } else if (quantifiedRestriction instanceof OWLObjectAllValuesFrom) {
                        System.out.println("    Restriction: allValuesFrom");
                    }
                    System.out.println("    Filler: " + filler);
                    printFillerDetails(filler, ontology, dataFactory);
                } else if (restriction instanceof OWLHasValueRestriction) {
                    OWLHasValueRestriction hasValueRestriction = (OWLHasValueRestriction) restriction;
                    OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) hasValueRestriction.getProperty();
                    OWLIndividual value = (OWLIndividual) hasValueRestriction.getFiller();

                    System.out.println("  Property: " + property.asOWLObjectProperty().getIRI());
                    System.out.println("    Restriction: hasValue");
                    System.out.println("    Value: " + value);
                    printValueDetails(value, ontology, dataFactory);
                }
            }
        }
    }

    private static void printFillerDetails(OWLClassExpression filler, OWLOntology ontology, OWLDataFactory dataFactory) {
        if (filler.isAnonymous()) {
            System.out.println("    Filler (Anonymous): " + filler);
        } else {
            System.out.println("    Filler (Named Class): " + filler.asOWLClass().getIRI());
        }
    }

    private static void printValueDetails(OWLIndividual value, OWLOntology ontology, OWLDataFactory dataFactory) {
        if (value.isAnonymous()) {
            System.out.println("    Value (Anonymous Individual): " + value);
        } else {
            System.out.println("    Value (Named Individual): " + value.asOWLNamedIndividual().getIRI());
        }
    }

    private static void printLabels(OWLOntology ontology, OWLClass owlClass) {
        IRI classIRI = owlClass.getIRI();
        for (OWLAnnotationAssertionAxiom annotation : ontology.getAnnotationAssertionAxioms(classIRI)) {
            OWLAnnotationProperty property = annotation.getProperty();
            OWLAnnotationValue value = annotation.getValue();
            if (property.isLabel() || property.getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {
                if (value instanceof OWLLiteral) {
                    OWLLiteral literal = (OWLLiteral) value;
                    System.out.println("Label: " + literal.getLiteral() + " (" + literal.getLang() + ")");
                }
            }
        }
    }
}