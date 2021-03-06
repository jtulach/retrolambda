// Copyright © 2013-2014 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.retrolambda.interfaces;

public interface MethodRelocations {

    MethodRef getMethodLocation(MethodRef original);

    String getCompanionClass(String className);
}
