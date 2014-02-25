%module CRFPP 
%include exception.i 
%{ 
#include "crfpp.h" 
#include "encoder.h" 
%} 

%newobject surface; 

%exception { 
  try { $ action } 
  catch (char *e) { SWIG_exception (SWIG_RuntimeError, e); } 
  catch (const char *e) { SWIG_exception (SWIG_RuntimeError, (char*)e); } 
} 

%feature ("notabstract") CRFPP::Model; 
%feature("notabstract") CRFPP::Tagger; 
%feature("notabstract") CRFPP::Encoder; 

%ignore CRFPP::createModel; 
%ignore CRFPP::createModelFromArray; 
%ignore CRFPP::createTagger; 
%ignore CRFPP::getTaggerError; 
%ignore CRFPP::getLastError; 

%rename(CRFPPTrainer) CRFPP::Encoder; 

%ignore CRFPP::Encoder::learn; 

%extend CRFPP::Model { Model(const char *arg); } 
%extend CRFPP::Tagger { Tagger(const char *arg); } 

%{ 

void delete_CRFPP_Model (CRFPP::Model *t) { 
  delete t; 
  t = 0; 
} 

CRFPP::Model* new_CRFPP_Model(const char *arg) { 
  CRFPP::Model *tagger = CRFPP::createModel(arg); 
  if (!tagger) throw CRFPP::getLastError(); 
  return tagger; 
} 

void delete_CRFPP_Tagger (CRFPP::Tagger *t) { 
  delete t; 
  t = 0; 
} 

CRFPP::Tagger* new_CRFPP_Tagger (const char *arg) { 
  CRFPP::Tagger *tagger = CRFPP::createTagger(arg); 
  if (!tagger) throw CRFPP::getLastError(); 
  return tagger; 
} 

%} 

%extend CRFPP::Encoder { 
    void train(const char *templfile, 
                    const char *trainfile, 
                    const char *modelfile, 
                    bool textmodelfile, 
                    size_t maxitr, 
                    size_t freq, 
                    double eta, 
                    double C, 
                    unsigned short thread_num, 
                    unsigned short shrinking_size, 
                    int algorithm) 
    { 
        bool b = $self->learn(templfile, trainfile, modelfile, textmodelfile, maxitr, freq, eta, C, thread_num, shrinking_size, algorithm); 
        if (!b) { 
            throw $self->what(); 
        } 
    }; 
     
    void train(const char *templfile, 
                    const char *trainfile, 
                    const char *modelfile, 
                    unsigned short thread_num) 
    { 
        bool b = $self->learn(templfile, trainfile, modelfile, false, 10000, 1, 0.00001, 1.0, thread_num, 20, 0); 
        if (!b) { 
            throw $self->what(); 
        } 
    } 
     
}; 

%include ../crfpp.h 
%include ../encoder.h 
%include version.h
