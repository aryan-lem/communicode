import React, { useRef, useEffect } from 'react';
import Editor, { Monaco } from '@monaco-editor/react';
import { editor } from 'monaco-editor';

interface CodeEditorProps {
  code: string;
  language: string;
  onChange: (value: string) => void;
  readonly?: boolean;
}

const CodeEditor: React.FC<CodeEditorProps> = ({ code, language, onChange, readonly = false }) => {
  const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);

  const handleEditorDidMount = (editor: editor.IStandaloneCodeEditor, monaco: Monaco) => {
    editorRef.current = editor;
    
    // Configure editor options
    monaco.editor.defineTheme('comunicodeTheme', {
      base: 'vs-dark',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': '#1e1e2e',
        'editor.foreground': '#f8f8f2',
        'editorCursor.foreground': '#f8f8f2',
        'editor.lineHighlightBackground': '#2a2a3c',
        'editorLineNumber.foreground': '#6272a4',
        'editor.selectionBackground': '#44475a',
        'editor.wordHighlightBackground': '#6272a4'
      }
    });
    
    monaco.editor.setTheme('comunicodeTheme');
  };
  
  return (
    <div style={{ height: 'calc(100vh - 100px)', width: '100%' }}>
      <Editor
        height="100%"
        defaultLanguage="javascript"
        language={language}
        value={code}
        onChange={(value) => onChange(value || '')}
        options={{
          readOnly: readonly,
          minimap: { enabled: true },
          fontSize: 14,
          wordWrap: 'on',
          autoIndent: 'full',
          formatOnPaste: true,
          formatOnType: true,
          scrollBeyondLastLine: false,
          automaticLayout: true
        }}
        onMount={handleEditorDidMount}
      />
    </div>
  );
};

export default CodeEditor;