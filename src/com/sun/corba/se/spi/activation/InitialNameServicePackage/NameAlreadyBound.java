package com.sun.corba.se.spi.activation.InitialNameServicePackage;


/**
* com/sun/corba/se/spi/activation/InitialNameServicePackage/NameAlreadyBound.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /jenkins/workspace/8-2-build-macosx-x86_64/jdk8u261/295/corba/src/share/classes/com/sun/corba/se/spi/activation/activation.idl
* Thursday, June 18, 2020 6:39:16 AM GMT
*/

public final class NameAlreadyBound extends org.omg.CORBA.UserException
{

  public NameAlreadyBound ()
  {
    super(NameAlreadyBoundHelper.id());
  } // ctor


  public NameAlreadyBound (String $reason)
  {
    super(NameAlreadyBoundHelper.id() + "  " + $reason);
  } // ctor

} // class NameAlreadyBound
