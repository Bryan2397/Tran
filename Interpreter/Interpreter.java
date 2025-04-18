package Interpreter;

import AST.*;
import Tran.Parser;
import Tran.Token;

import java.util.*;

public class Interpreter {

    /** Constructor - get the interpreter ready to run. Set members from parameters and "prepare" the class.
     *
     * Store the tran node.
     * Add any built-in methods to the AST
     * @param top - the head of the AST
     */
    TranNode top;

    public Interpreter(TranNode top) {this.top = top;}

    /**
     * This is the public interface to the interpreter. After parsing, we will create an interpreter and call start to
     * start interpreting the code.
     *
     * Search the classes in Tran for a method that is "isShared", named "start", that is not private and has no parameters
     * Call "InterpretMethodCall" on that method, then return.
     * Throw an exception if no such method exists.
     */
    public void start() {
        // Find the "start" method
        throw new RuntimeException("No 'start' method found");
    }

    //              Running Methods

    /**
     * Find the method (local to this class, shared (like Java's system.out.print), or a method on another class)
     * Evaluate the parameters to have a list of values
     * Use interpretMethodCall() to actually run the method.
     *
     * Call GetParameters() to get the parameter value list
     * Find the method. This is tricky - there are several cases:
     * someLocalMethod() - has NO object name. Look in "object"
     * console.write() - the objectName is a CLASS and the method is shared
     * bestStudent.getGPA() - the objectName is a local or a member
     *
     * Once you find the method, call InterpretMethodCall() on it. Return the list that it returns.
     * Throw an exception if we can't find a match.
     * @param object - the object we are inside right now (might be empty)
     * @param locals - the current local variables
     * @param mc - the method call
     * @return - the return values
     */
    private List<InterpreterDataType> findMethodForMethodCallAndRunIt(Optional<ObjectIDT> object, HashMap<String, InterpreterDataType> locals, MethodCallStatementNode mc) {
        List<InterpreterDataType> result = null;

        if(locals.containsKey("console")){
            List<InterpreterDataType> parameters = getParameters(object, locals, mc);
            result = interpretMethodCall(object, , parameters);
            return result;
        }
        if(mc.objectName.isPresent()){
            for(int i = 0; i < object.get().astNode.methods.size(); i++){
                if(mc.methodName.equals(object.get().astNode.methods.get(i).name)){
                    List<InterpreterDataType> parameters = getParameters(object, locals, mc);
                    result = interpretMethodCall(object, object.get().astNode.methods.get(i), parameters);
                    return result;
                }
            }
        } else if (locals.containsKey(mc.objectName.get())) {
            List<InterpreterDataType> parameters = getParameters(object, locals, mc);
            result = interpretMethodCall(object, object.get().astNode.methods.get(0), parameters);
            return result;
        } else if (getClassByName(mc.objectName.get()).isPresent()) {
            List<InterpreterDataType> parameters = getParameters(object, locals, mc);
            ObjectIDT a = new ObjectIDT(object.get().astNode);
            result = interpretMethodCall(object, getMethodFromObject(a, mc, parameters), parameters);
            return result;

        }else {
            throw new RuntimeException("exception from findMethodForMethodCall");
        }

        return result;
    }

    /**
     * Run a "prepared" method (found, parameters evaluated)
     * This is split from findMethodForMethodCallAndRunIt() because there are a few cases where we don't need to do the finding:
     * in start() and dealing with loops with iterator objects, for example.
     *
     * Check to see if "m" is a built-in. If so, call Execute() on it and return
     * Make local variables, per "m"
     * If the number of passed in values doesn't match m's "expectations", throw
     * Add the parameters by name to locals.
     * Call InterpretStatementBlock
     * Build the return list - find the names from "m", then get the values for those names and add them to the list.
     * @param object - The object this method is being called on (might be empty for shared)
     * @param m - Which method is being called
     * @param values - The values to be passed in
     * @return the returned values from the method
     */
    private List<InterpreterDataType> interpretMethodCall(Optional<ObjectIDT> object, MethodDeclarationNode m, List<InterpreterDataType> values) {
        var retVal = new LinkedList<InterpreterDataType>();
        HashMap<String, InterpreterDataType> local = new HashMap<>();

        for(int i = 0; i < m.locals.size(); i++){
        local.put(m.locals.get(i).name, instantiate(m.locals.get(i).type));
        }
        if(m.parameters.size() != values.size()){
        throw new RuntimeException("locals size and values size aren't equal");
        }
        for(int i = 0; i < values.size();i++){
            local.put(values.get(i).toString(), values.get(i));
        }
        for(int i = 0; i < local.size(); i++){
            retVal.add(local.get(m.locals.get(i).name));
        }
        interpretStatementBlock(object, m.statements, local);
        return retVal;
    }

    //              Running Constructors

    /**
     * This is a special case of the code for methods. Just different enough to make it worthwhile to split it out.
     *
     * Call GetParameters() to populate a list of IDT's
     * Call GetClassByName() to find the class for the constructor
     * If we didn't find the class, throw an exception
     * Find a constructor that is a good match - use DoesConstructorMatch()
     * Call InterpretConstructorCall() on the good match
     * @param callerObj - the object that we are inside when we called the constructor
     * @param locals - the current local variables (used to fill parameters)
     * @param mc  - the method call for this construction
     * @param newOne - the object that we just created that we are calling the constructor for
     */
    private void findConstructorAndRunIt(Optional<ObjectIDT> callerObj, HashMap<String, InterpreterDataType> locals, MethodCallStatementNode mc, ObjectIDT newOne) {
        List<InterpreterDataType> a = getParameters(callerObj, locals, mc);
        if(mc.objectName.isEmpty()){
            throw new RuntimeException();
        }
        Optional<ClassNode> b = getClassByName(mc.objectName.get());
        if(b.isEmpty()){
            throw new RuntimeException();
        }
        for(int i = 0; i < newOne.astNode.constructors.size(); i++){
            if(doesConstructorMatch(newOne.astNode.constructors.get(i), mc, a)){
                interpretConstructorCall(newOne, newOne.astNode.constructors.get(i), a);
                break;
            }
        }

    }

    /**
     * Similar to interpretMethodCall, but "just different enough" - for example, constructors don't return anything.
     *
     * Creates local variables (as defined by the ConstructorNode), calls Instantiate() to do the creation
     * Checks to ensure that the right number of parameters were passed in, if not throw.
     * Adds the parameters (with the names from the ConstructorNode) to the locals.
     * Calls InterpretStatementBlock
     * @param object - the object that we allocated
     * @param c - which constructor is being called
     * @param values - the parameter values being passed to the constructor
     */
    private void interpretConstructorCall(ObjectIDT object, ConstructorNode c, List<InterpreterDataType> values) {
        HashMap<String,InterpreterDataType> a = new HashMap<>();
        if(c.parameters.size() != values.size()){
            throw new RuntimeException();
        }

        for(int i = 0; i < c.locals.size(); i++){
            a.put(c.locals.get(i).name, instantiate(c.locals.get(i).type));
        }
        for(int i = 0; i < object.astNode.members.size(); i++){
            object.members.put(object.astNode.members.get(i).declaration.name,instantiate(object.astNode.members.get(i).declaration.type));
        }
        interpretStatementBlock(Optional.of(object), c.statements, a);

    }

    //              Running Instructions

    /**
     * Given a block (which could be from a method or an "if" or "loop" block, run each statement.
     * Blocks, by definition, do ever statement, so iterating over the statements makes sense.
     *
     * For each statement in statements:
     * check the type:
     *      For AssignmentNode, FindVariable() to get the target. Evaluate() the expression. Call Assign() on the target with the result of Evaluate()
     *      For MethodCallStatementNode, call doMethodCall(). Loop over the returned values and copy the into our local variables
     *      For LoopNode - there are 2 kinds.
     *          Setup:
     *          If this is a Loop over an iterator (an Object node whose class has "iterator" as an interface)
     *              Find the "getNext()" method; throw an exception if there isn't one
     *          Loop:
     *          While we are not done:
     *              if this is a boolean loop, Evaluate() to get true or false.
     *              if this is an iterator, call "getNext()" - it has 2 return values. The first is a boolean (was there another?), the second is a value
     *              If the loop has an assignment variable, populate it: for boolean loops, the true/false. For iterators, the "second value"
     *              If our answer from above is "true", InterpretStatementBlock() on the body of the loop.
     *       For If - Evaluate() the condition. If true, InterpretStatementBlock() on the if's statements. If not AND there is an else, InterpretStatementBlock on the else body.
     * @param object - the object that this statement block belongs to (used to get member variables and any members without an object)
     * @param statements - the statements to run
     * @param locals - the local variables
     */
    private void interpretStatementBlock(Optional<ObjectIDT> object, List<StatementNode> statements, HashMap<String, InterpreterDataType> locals) {
        for(int i = 0; i < statements.size(); i++){
            if(statements.get(i) instanceof AssignmentNode){
               var a = findVariable(statements.get(i).toString(),locals, object);
                var b = evaluate(locals,object, ((AssignmentNode) statements.get(i)).expression);
                b.Assign(a);
            }
            if(statements.get(i) instanceof MethodCallStatementNode){
                List<InterpreterDataType> list = new ArrayList<>();
                for(int j = 0; j < ((MethodCallStatementNode) statements.get(j)).returnValues.size(); j++){
                    
                }
            }
        }
    }

    /**
     *  evaluate() processes everything that is an expression - math, variables, boolean expressions.
     *  There is a good bit of recursion in here, since math and comparisons have left and right sides that need to be evaluated.
     *
     * See the How To Write an Interpreter document for examples
     * For each possible ExpressionNode, do the work to resolve it:
     * BooleanLiteralNode - create a new BooleanLiteralNode with the same value
     *      - Same for all of the basic data types
     * BooleanOpNode - Evaluate() left and right, then perform either and/or on the results.
     * CompareNode - Evaluate() both sides. Do good comparison for each data type
     * MathOpNode - Evaluate() both sides. If they are both numbers, do the math using the built-in operators. Also handle String + String as concatenation (like Java)
     * MethodCallExpression - call doMethodCall() and return the first value
     * VariableReferenceNode - call findVariable()
     * @param locals the local variables
     * @param object - the current object we are running
     * @param expression - some expression to evaluate
     * @return a value
     */
    private InterpreterDataType evaluate(HashMap<String, InterpreterDataType> locals, Optional<ObjectIDT> object, ExpressionNode expression) {
        if(expression instanceof BooleanLiteralNode){
            if(((BooleanLiteralNode) expression).value){
                BooleanLiteralNode a = new BooleanLiteralNode(true);
                return new BooleanIDT(a.value);
            }
            BooleanLiteralNode a = new BooleanLiteralNode(false);
            return new BooleanIDT(a.value);

        } else if (expression instanceof NumericLiteralNode) {
            NumericLiteralNode a = new NumericLiteralNode();
            a.value = ((NumericLiteralNode) expression).value;
            return new NumberIDT(a.value);

        } else if (expression instanceof StringLiteralNode) {
            StringLiteralNode a = new StringLiteralNode();
            a.value = ((StringLiteralNode) expression).value;
            return new StringIDT(a.value);

        } else if (expression instanceof CharLiteralNode) {
            CharLiteralNode a = new CharLiteralNode();
            a.value = ((CharLiteralNode) expression).value;
            return new CharIDT(a.value);

        } else if (expression instanceof VariableReferenceNode) {
            VariableReferenceNode a = new VariableReferenceNode();
            a.name = ((VariableReferenceNode) expression).name;
            return findVariable(a.toString(), locals, object);

        } else if (expression instanceof CompareNode) {
            var a = evaluate(locals, object, ((CompareNode) expression).left);
            var b = evaluate(locals, object, ((CompareNode) expression).right);
            if(!a.getClass().equals(b.getClass())){
                throw new IllegalArgumentException();
            }
            if(b instanceof StringIDT && a instanceof StringIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.eq){
                int c = a.toString().compareTo(b.toString());
                if(c == 0){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof StringIDT && b instanceof StringIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.gt){
                int c = a.toString().compareTo(b.toString());
                if(c > 0){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof StringIDT && b instanceof StringIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.ge){
                int c = a.toString().compareTo(b.toString());
                if(c >= 0){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof StringIDT && b instanceof StringIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.lt){
                int c = a.toString().compareTo(b.toString());
                if(c < 0){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof StringIDT && b instanceof StringIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.le){
                int c = a.toString().compareTo(b.toString());
                if(c <= 0){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof StringIDT && b instanceof StringIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.ne){
                int c = a.toString().compareTo(b.toString());
                if(c != 0){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof CharIDT && b instanceof CharIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.eq){
                if(((CharIDT)a).Value == ((CharIDT)b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof CharIDT && b instanceof CharIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.ne){
                if(((CharIDT)a).Value != ((CharIDT)b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }

            if(a instanceof BooleanIDT && b instanceof BooleanIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.eq){
                if(a.toString().equals(b.toString())){
                return new BooleanIDT(true);
                } else if (a.equals(false) && b.equals(false)) {
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof BooleanIDT && b instanceof BooleanIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.ne){
                if(!a.toString().equals(b.toString())){
                    return new BooleanIDT(true);
                } else if (a.equals(false) && b.equals(false)) {
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.eq){
                if(((NumberIDT) a).Value == ((NumberIDT) b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.ne){
                if(((NumberIDT) a).Value != ((NumberIDT) b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.ge){
                if(((NumberIDT) a).Value >= ((NumberIDT) b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.gt){
                if(((NumberIDT) a).Value > ((NumberIDT) b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.lt){
                if(((NumberIDT) a).Value < ((NumberIDT) b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((CompareNode) expression).op == CompareNode.CompareOperations.le){
                if(((NumberIDT) a).Value <= ((NumberIDT) b).Value){
                    return new BooleanIDT(true);
                }else{
                    return new BooleanIDT(false);
                }
            }

        }else if(expression instanceof MathOpNode){
            var a = evaluate(locals, object, ((MathOpNode) expression).left);
            var b = evaluate(locals, object, ((MathOpNode) expression).right);

            if(a instanceof NumberIDT && b instanceof NumberIDT && ((MathOpNode) expression).op == MathOpNode.MathOperations.add){
                return new NumberIDT(((NumberIDT) a).Value+((NumberIDT) b).Value);
            }
            if(a instanceof StringIDT && b instanceof StringIDT && ((MathOpNode) expression).op == MathOpNode.MathOperations.add){
                return new StringIDT(((StringIDT) a).Value.concat(((StringIDT) b).Value));
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((MathOpNode) expression).op == MathOpNode.MathOperations.subtract){
                return new NumberIDT(((NumberIDT) a).Value-((NumberIDT) b).Value);
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((MathOpNode) expression).op == MathOpNode.MathOperations.multiply){
                return new NumberIDT(((NumberIDT) a).Value*((NumberIDT) b).Value);
            }
            if(a instanceof NumberIDT && b instanceof NumberIDT && ((MathOpNode) expression).op == MathOpNode.MathOperations.divide){
                return new NumberIDT(((NumberIDT) a).Value/((NumberIDT) b).Value);
            }

            if(a instanceof NumberIDT && b instanceof NumberIDT && ((MathOpNode) expression).op == MathOpNode.MathOperations.modulo){
                return new NumberIDT(((NumberIDT) a).Value%((NumberIDT) b).Value);
            }
        }else if(expression instanceof MethodCallExpressionNode){
            List<InterpreterDataType> a = new ArrayList<>();
            for(int i = 0; i < ((MethodCallExpressionNode) expression).parameters.size(); i++){
                a.add(evaluate(locals, object, ((MethodCallExpressionNode) expression).parameters.get(i)));
            }
            MethodCallStatementNode n = new MethodCallStatementNode((MethodCallExpressionNode) expression);
            ;
        }
        throw new IllegalArgumentException();
    }

    //              Utility Methods

    /**
     * Used when trying to find a match to a method call. Given a method declaration, does it match this methoc call?
     * We double check with the parameters, too, although in theory JUST checking the declaration to the call should be enough.
     *
     * Match names, parameter counts (both declared count vs method call and declared count vs value list), return counts.
     * If all of those match, consider the types (use TypeMatchToIDT).
     * If everything is OK, return true, else return false.
     * Note - if m is a built-in and isVariadic is true, skip all of the parameter validation.
     * @param m - the method declaration we are considering
     * @param mc - the method call we are trying to match
     * @param parameters - the parameter values for this method call
     * @return does this method match the method call?
     */
    private boolean doesMatch(MethodDeclarationNode m, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        if(m.name.equals(mc.methodName)){
            return false;
        }
        for(int i = 0; i < m.parameters.size(); i++){
            if(m.parameters.get(i) != mc.parameters.get(i)) {
                return false;
            }
            if(!typeMatchToIDT(m.parameters.get(i).type, parameters.get(i))){
                return false;
            }
        }
        if(m.returns.size() != mc.returnValues.size()){
            return false;
        }
        for (int i = 0; i < mc.returnValues.size(); i++){
            if(!typeMatchToIDT(mc.returnValues.get(i).name , instantiate(m.returns.get(i).name))){
               return false;
            }
        }
        return true;
    }

    /**
     * Very similar to DoesMatch() except simpler - there are no return values, the name will always match.
     * @param c - a particular constructor
     * @param mc - the method call
     * @param parameters - the parameter values
     * @return does this constructor match the method call?
     */
    private boolean doesConstructorMatch(ConstructorNode c, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        if(mc.parameters.size() != c.parameters.size() && c.parameters.size() != parameters.size()){
            return false;
        }
        for(int i = 0; i < parameters.size(); i++){
            if(!typeMatchToIDT(c.parameters.get(i).type, parameters.get(i))){
                return false;
            }
        }
        return true;
    }

    /**
     * Used when we call a method to get the list of values for the parameters.
     *
     * for each parameter in the method call, call Evaluate() on the parameter to get an IDT and add it to a list
     * @param object - the current object
     * @param locals - the local variables
     * @param mc - a method call
     * @return the list of method values
     */
    private List<InterpreterDataType> getParameters(Optional<ObjectIDT> object, HashMap<String,InterpreterDataType> locals, MethodCallStatementNode mc) {
        List<InterpreterDataType> a = new ArrayList<>();
        for(int i = 0; i < mc.parameters.size(); i++){
            a.add(evaluate(locals, object, mc.parameters.get(i)));
        }
        return a;
    }

    /**
     * Used when we have an IDT and we want to see if it matches a type definition
     * Commonly, when someone is making a function call - do the parameter values match the method declaration?
     *
     * If the IDT is a simple type (boolean, number, etc) - does the string type match the name of that IDT ("boolean", etc)
     * If the IDT is an object, check to see if the name matches OR the class has an interface that matches
     * If the IDT is a reference, check the inner (refered to) type
     * @param type the name of a data type (parameter to a method)
     * @param idt the IDT someone is trying to pass to this method
     * @return is this OK?
     */
    private boolean typeMatchToIDT(String type, InterpreterDataType idt) {
        if(idt instanceof BooleanIDT && type.equals("boolean")){
            return true;
        } else if (idt instanceof NumberIDT && type.equals("number")) {
            return true;
        } else if (idt instanceof CharIDT && type.equals("character")) {
            return true;
        } else if (idt instanceof ReferenceIDT && type.equals(((ReferenceIDT) idt).refersTo.get().astNode.name)) {
            return true;
        } else if (idt instanceof ObjectIDT) {
            return true;
        }
        throw new RuntimeException("Unable to resolve type " + type);
    }

    /**
     * Find a method in an object that is the right match for a method call (same name, parameters match, etc. Uses doesMatch() to do most of the work)
     *
     * Given a method call, we want to loop over the methods for that class, looking for a method that matches (use DoesMatch) or throw
     * @param object - an object that we want to find a method on
     * @param mc - the method call
     * @param parameters - the parameter value list
     * @return a method or throws an exception
     */
    private MethodDeclarationNode getMethodFromObject(ObjectIDT object, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        for(int i = 0; i < object.astNode.methods.size(); i++){
            if(doesMatch(object.astNode.methods.get(i), mc, parameters)){
                return object.astNode.methods.get(i);
            }
        }
        throw new RuntimeException("Unable to resolve method call " + mc);
    }

    /**
     * Find a class, given the name. Just loops over the TranNode's classes member, matching by name.
     *
     * Loop over each class in the top node, comparing names to find a match.
     * @param name Name of the class to find
     * @return either a class node or empty if that class doesn't exist
     */
    private Optional<ClassNode> getClassByName(String name) {
        for(int i = 0; i < top.Classes.size(); i++){
            if(top.Classes.get(i).name.equals(name)){
                return Optional.ofNullable(top.Classes.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Given an execution environment (the current object, the current local variables), find a variable by name.
     *
     * @param name  - the variable that we are looking for
     * @param locals - the current method's local variables
     * @param object - the current object (so we can find members)
     * @return the IDT that we are looking for or throw an exception
     */
    private InterpreterDataType findVariable(String name, HashMap<String,InterpreterDataType> locals, Optional<ObjectIDT> object) {
        if (locals.containsKey(name)) {
            var a = locals.get(name);
            return a;
        }
        if(object.isPresent()) {
            if(object.get().members.containsKey(name)){
                var c = object.get().members.get(name);
                return c;
            }else {
                throw new RuntimeException("Unable to find variable " + name);
            }
        }
        throw new RuntimeException("Unable to find variable " + name);

    }

    /**
     * Given a string (the type name), make an IDT for it.
     *
     * @param type The name of the type (string, number, boolean, character). Defaults to ReferenceIDT if not one of those.
     * @return an IDT with default values (0 for number, "" for string, false for boolean, ' ' for character)
     */
    private InterpreterDataType instantiate(String type) {
        if(type.equals("string")){
            StringIDT a = new StringIDT("");
            return a;
        } else if (type.equals("number")) {
            NumberIDT a = new NumberIDT(0);
            return a;
        } else if (type.equals("boolean")) {
            BooleanIDT a = new BooleanIDT(false);
            return a;
        } else if (type.equals("character")) {
            CharIDT a = new CharIDT(' ');
            return a;
        }else {
            ReferenceIDT a = new ReferenceIDT();
            return a;
        }
    }
}
