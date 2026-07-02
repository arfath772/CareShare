const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
  email: {
    type: String,
    unique: true,
    required: true,
    lowercase: true,
    validate: { validator: (v) => /^\S+@\S+\.\S+$/.test(v), message: 'Invalid email' }
  },
  password: { type: String, required: true },
  firstName: { type: String, required: true },
  lastName: { type: String, required: true },
  phoneNumber: String,
  phone: String,
  roles: { type: [String], default: ['ROLE_USER'] },
  resetToken: { type: String },
  resetTokenExpiry: { type: Date },
  isAdmin: { type: Boolean, default: false },
  accountType: { type: String, enum: ['USER', 'NGO'], default: 'USER' },
  ngoStatus: { type: String, enum: ['NOT_REQUIRED', 'PENDING', 'APPROVED', 'REJECTED'], default: 'NOT_REQUIRED' },
  ngoLegalName: String,
  ngoDarpanId: String,
  ngoPan: String,
  ngoOfficeAddress: String,
  ngoRejectionReason: String,
  ngoDocuments: { type: Object, default: {} }
}, { timestamps: true });

// Hash password before saving
userSchema.pre('save', async function(next) {
  if (!this.isModified('password')) return next();
  this.password = await bcrypt.hash(this.password, 10);
  next();
});

// Instance methods
userSchema.methods.validatePassword = async function(password) {
  return await bcrypt.compare(password, this.password);
};

userSchema.methods.toJSON = function() {
  const obj = this.toObject();
  obj.id = obj._id.toString();

  if (obj.ngoDocuments && typeof obj.ngoDocuments === 'object') {
    const sanitizedDocs = {};
    Object.entries(obj.ngoDocuments).forEach(([key, value]) => {
      if (typeof value === 'string') {
        sanitizedDocs[key] = value;
        return;
      }

      if (value && typeof value === 'object') {
        if (value.storage === 'mongodb' || value.data) {
          sanitizedDocs[key] = {
            storage: 'mongodb',
            filename: value.filename || key,
            contentType: value.contentType || 'application/octet-stream',
            url: `/api/auth/ngo-documents/${obj.id}/${key}`
          };
          return;
        }
      }
    });
    obj.ngoDocuments = sanitizedDocs;
  }

  delete obj.password;
  delete obj.resetToken;
  delete obj.resetTokenExpiry;
  return obj;
};

module.exports = mongoose.model('User', userSchema);
