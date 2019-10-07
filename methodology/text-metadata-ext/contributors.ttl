# original source location:
# /db/apps/84000-data/config/contributor-types.xml

eft:TranslationContributor
  a owl:Class ;
  rdfs:label "There are a handfull of instances of this class, representing different roles of involvement with a translation of a particular text"@en ;
  .

eft:Author
  a owl:Class ;
  rdfs:subClassOf eft:TranslationContributor ;
  rdfs:label "An Author is a contributor who creates new content (as opposed to editing existing one) in the target language"@en ;
  .

eft:Editor
  a owl:Class ;
  rdfs:subClassOf eft:TranslationContributor ;
  rdfs:label "An Editor is a contributor who works on the output of an Author"@en ;
  .

eft:Consultant
  a owl:Class ;
  rdfs:subClassOf eft:TranslationContributor ;
  rdfs:label "A Consultan is a contributor, usually an expert in the source language and domain, who Authors and Editors may consult in order to do their work better"@en ;
  .

##########################################################
# instances of Author, Editor and Consultant start here: #
##########################################################

eft:author/translatorEng
  a eft:Author
  rdfs:label "English Translator"@en;
  .

eft:author/preface
  a eft:Author
  rdfs:label "Preface Author"@en;
  .

eft:consultant/advisor
  a eft:Consultant
  rdfs:label "Advising Consultant"@en;
  .

eft:editor/reviser
  a eft:Editor
  rdfs:label "Translation Team Editor"@en;
  .

eft:editor/associateEditor
  a eft:Editor
  rdfs:label "Associate Editor"@en;
  .

eft:editor/externalReviewer
  a eft:Editor
  rdfs:label "External Reviewer"@en;
  .

eft:editor/copyEditor
  a eft:Editor
  rdfs:label "Copy Editor"@en;
  .

eft:editor/TEImarkupEditor
  a eft:Editor
  rdfs:label "Markup Editor"@en;
  .

eft:editor/proofreader
  a eft:Editor
  rdfs:label "Proofreader"@en;
  .

eft:editor/finalReviewer
  a eft:Editor
  rdfs:label "Final Editor"@en;
  .
