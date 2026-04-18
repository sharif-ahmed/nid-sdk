import struct
import os

def check_alignment(file_path):
    print(f"Checking: {file_path}")
    with open(file_path, 'rb') as f:
        # Read ELF header
        e_ident = f.read(16)
        if e_ident[:4] != b'\x7fELF':
            print("Not an ELF file")
            return
        
        # 64-bit ELF (assuming arm64-v8a or x86_64)
        is_64 = e_ident[4] == 2
        
        f.seek(32 if is_64 else 28)
        phoff = struct.unpack('<Q' if is_64 else '<I', f.read(8 if is_64 else 4))[0]
        
        f.seek(54 if is_64 else 42)
        phentsize = struct.unpack('<H', f.read(2))[0]
        phnum = struct.unpack('<H', f.read(2))[0]
        
        max_align = 0
        for i in range(phnum):
            f.seek(phoff + i * phentsize)
            # p_type is at offset 0
            p_type = struct.unpack('<I', f.read(4))[0]
            
            if p_type == 1: # PT_LOAD
                # p_align is at different offsets for 32/64 bit
                if is_64:
                    # 64-bit Program Header:
                    # 0: type (4), 4: flags (4), 8: offset (8), 16: vaddr (8), 
                    # 24: paddr (8), 32: filesz (8), 40: memsz (8), 48: align (8)
                    f.seek(phoff + i * phentsize + 48)
                    align = struct.unpack('<Q', f.read(8))[0]
                else:
                    # 32-bit Program Header:
                    # 0: type (4), 4: offset (4), 8: vaddr (4), 12: paddr (4),
                    # 16: filesz (4), 20: memsz (4), 24: flags (4), 28: align (4)
                    f.seek(phoff + i * phentsize + 28)
                    align = struct.unpack('<I', f.read(4))[0]
                
                print(f"  Segment {i} align: {align}")
                max_align = max(max_align, align)
        
        if max_align >= 16384:
            print(f"Result: SUCCESS (Aligned to {max_align})")
        else:
            print(f"Result: FAIL (Aligned to {max_align})")

apk_lib_dir = "app/src/main/jniLibs/arm64-v8a"
for filename in os.listdir(apk_lib_dir):
    if filename.endswith(".so"):
        check_alignment(os.path.join(apk_lib_dir, filename))
