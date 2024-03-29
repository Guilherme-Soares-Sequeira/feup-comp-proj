package pt.up.fe.comp2023;

import pt.up.fe.comp2023.node.information.Method;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2023.utils.SymbolInfo;
import pt.up.fe.comp2023.utils.SymbolPosition;
import pt.up.fe.specs.util.collections.*;

import javax.swing.text.html.Option;
import java.util.*;

public class SymbolTable implements pt.up.fe.comp.jmm.analysis.table.SymbolTable {
    final private SpecsList<Method> methods;
    private SpecsList<String> importedClasses;
    final private List<String> imports;
    private List<Symbol> fields;
    private String className;
    private String superClassName;
    private Method currentMethod;

    public SymbolTable() {
        this.methods = SpecsList.newInstance(Method.class);
        this.importedClasses = SpecsList.newInstance(String.class);
        this.imports = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.className = "";
        this.superClassName = "";
    }

    @Override
    public List<String> getImports() {
        return this.imports;
    }

    public void addImport(String s) {
        imports.add(s);
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getSuper() {
        return this.superClassName.isEmpty() ? null : this.superClassName;
    }

    public void setSuper(String superClassName) {
        this.superClassName = superClassName;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields.isEmpty() ? null : this.fields;
    }

    public void setFields(List<Symbol> fields) {
        this.fields = fields;
    }

    @Override
    public List<String> getMethods() {
        List<String> methods = this.methods.stream().map(Method::getName).toList();
        return (methods.isEmpty() ? null : methods);
    }

    public SpecsList<Method> getFullMethods() {
        return this.methods;
    }

    @Override
    public Type getReturnType(String s) {
        for (Method method : this.methods) {
            if (method.getName().equals(s)) {
                return method.getRetType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String s) {
        for (Method method : this.methods) {
            if (method.getName().equals(s)) {
                return method.getArguments();
            }
        }
        return null;
    }

    public void addMethod(String name, String returnType) {
        Method currentMethod = new Method(name, returnType);
        this.methods.add(currentMethod);
    }

    public void addMethod(String methodName, Type type) {
        this.methods.add(new Method(methodName, type));
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }
    @Override
    public List<Symbol> getLocalVariables(String s) {
        for (Method method : methods) {
            if (method.getName().equals(s)) {
                return method.getVariables();
            }
        }
        return null;
    }

    public Optional<Method> getMethodTry(String s) {
        for (Method method : this.methods) {
            if (method.getName().equals(s)) return Optional.of(method);
        }
        return Optional.empty();
    }

    public Method getMethodOrWarn(String methodName, String callerName) {
        Optional<Method> methodOpt = getMethodTry(methodName);
        if (methodOpt.isEmpty()) {
            System.err.println("[" + callerName + "]: Tried to find a method by the name of " + methodName +
                " but it couldn't be found in the symbol table.");
            return null;
        }
        return methodOpt.get();
    }

    public void addField(Symbol field) {
        this.fields.add(field);
    }

    public SymbolInfo getMostSpecificSymbol(String methodName, String symbolName) {
        Optional<Method> methodOpt = this.getMethodTry(methodName);

        if (methodOpt.isEmpty()) {
            System.err.println("Tried to find method with name '" + methodName + "' but it wasn't found");
            return null;
        }

        Method method = methodOpt.get();

        for (Symbol symbol : method.getVariables()) {
            if (symbol.getName().equals(symbolName)) return new SymbolInfo(symbol, SymbolPosition.LOCAL);
        }

        for (Symbol symbol : method.getArguments()) {
            if (symbol.getName().equals(symbolName)) return new SymbolInfo(symbol, SymbolPosition.PARAM);
        }

        for (Symbol symbol : this.fields) {
            if (symbol.getName().equals(symbolName)) return new SymbolInfo(symbol, SymbolPosition.FIELD);
        }


        System.err.println("Tried to find variable with name '" + symbolName + "' but it wasn't found.");
        return null;
    }

    public Optional<SymbolInfo> getMostSpecificSymbolTry(String methodName, String symbolName) {
        Optional<Method> methodOpt = this.getMethodTry(methodName);

        if (methodOpt.isEmpty()) {
            System.err.println("Tried to find method with name '" + methodName + "' but it wasn't found");
            return Optional.empty();
        }

        Method method = methodOpt.get();

        for (Symbol symbol : method.getVariables()) {
            if (symbol.getName().equals(symbolName)) return Optional.of(new SymbolInfo(symbol, SymbolPosition.LOCAL));
        }

        for (Symbol symbol : method.getArguments()) {
            if (symbol.getName().equals(symbolName)) return Optional.of(new SymbolInfo(symbol, SymbolPosition.PARAM));
        }

        for (Symbol symbol : this.fields) {
            if (symbol.getName().equals(symbolName)) return Optional.of(new SymbolInfo(symbol, SymbolPosition.FIELD));
        }

        return Optional.empty();
    }

    public Boolean symbolIsDeclared(String parentMethodName, String symbolName, String currentAuxVarName) {
        Optional<Method> methodOpt = this.getMethodTry(parentMethodName);

        if (methodOpt.isPresent()) {
            Method method = methodOpt.get();
            for (Symbol symbol : method.getVariables()) {
                if (symbol.getName().equals(symbolName)) return true;
            }

            for (Symbol symbol : method.getArguments()) {
                if (symbol.getName().equals(symbolName)) return true;
            }
        }

        for (Symbol symbol : this.fields) {
            if (symbol.getName().equals(symbolName)) return true;
        }

        if (symbolName.startsWith("aux")) {
            int currentAuxVarNumber = Integer.parseInt(currentAuxVarName.substring(3));
            int symbolAuxVarNumber = Integer.parseInt(symbolName.substring(3));
            return symbolAuxVarNumber <= currentAuxVarNumber;
        }

        return false;
    }


    public SpecsList<String> getImportedClasses() {
        return importedClasses;
    }

    public void addImportedClass(String importedClass) {
        this.importedClasses.add(importedClass);
    }
}
