This package contains the MIF 2.1 schemas that are deemed "ready for release" by the HL7 Tooling committee.

Some of the contained schemas are considere "stable" models where there is significant tooling and implementation experience and where errors in design are unlikely (though still possible).  Significant changes in these schemas will not occur except as part of a new major release.
It also encompasses schemas considered to be "beta".  These schemas are being used for development and have been thoroughly reviewed, but there is not yet sufficient implementer experience to be confident they have all of their kinks worked out.  Substantive changes are possible, but only in response to implementer feedback that the existing structure is somehow unworkable and with the consent of those early adopters registered as users of these schemas.
For this reason, we ask that any individual making use of "beta" schemas to please make an announcement of this fact to the HL7 Tooling list server (tooling@lists.hl7.org) so that we can consult with you in the event a substantive change is proposed.
Schemas falling into the beta category are:
mif-core-changes.xsd
mif-model-crossReference.xsd
mif-model-datatype.xsd
mif-model-documentation.xsd
mif-model-dynamic.xsd
mif-model-interface.xsd
mif-model-publication.xsd
mif-model-staticDerived.xsd
mif-model-vocabulary.xsd

In addition to the schemas published here, there are a series of schemas considered to be in "alpha" or "development" state.  They have not been published as part of the release, but are available from the tooling.  Should an implementer wish to make use of them, the Tooling Committee will be happy to accept a proposal to migrate them to "beta" status.  These schemas cover the following areas:
- annotation libraries (used for maintaining shared annotations that can be cascaded through models and other artifacts)
- conformance (used to document conformance profiles)
- packaging (used for conveying collections of other artifacts)
- requirements (used to represent storyboards, domain analysis models and other requirements artifacts)
- testing (used to represnt test cases and test collections)